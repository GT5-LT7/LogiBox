package com.sparta.gt5lt7.logisticsservice.application;

import com.sparta.gt5lt7.common.entity.UserRole;
import com.sparta.gt5lt7.common.security.CustomUserPrincipal;
import com.sparta.gt5lt7.logisticsservice.domain.entity.DeliveryAgent;
import com.sparta.gt5lt7.logisticsservice.domain.entity.Hub;
import com.sparta.gt5lt7.logisticsservice.domain.entity.type.AgentType;
import com.sparta.gt5lt7.logisticsservice.domain.repository.DeliveryAgentRepository;
import com.sparta.gt5lt7.logisticsservice.domain.repository.DeliveryRepository;
import com.sparta.gt5lt7.logisticsservice.domain.repository.HubRepository;
import com.sparta.gt5lt7.logisticsservice.global.exception.DeliveryAgentErrorCode;
import com.sparta.gt5lt7.logisticsservice.global.exception.DeliveryAgentException;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubErrorCode;
import com.sparta.gt5lt7.logisticsservice.global.exception.HubException;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.DeliveryAgentRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.DeliveryAgentSearchRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.request.DeliveryAgentUpdateRequest;
import com.sparta.gt5lt7.logisticsservice.presentation.dto.response.DeliveryAgentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeliveryAgentServiceTest {

    @Mock
    private DeliveryAgentRepository deliveryAgentRepository;

    @Mock
    private HubRepository hubRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private DeliveryAgentService deliveryAgentService;

    private UUID userId;
    private UUID hubId;
    private UUID agentId;
    private Hub hub;
    private DeliveryAgent hubAgent;
    private DeliveryAgent companyAgent;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        hubId = UUID.randomUUID();
        agentId = UUID.randomUUID();

        hub = Hub.builder()
                .hubId(hubId)
                .name("서울허브")
                .address("서울시")
                .latitude(37.5)
                .longitude(127.0)
                .build();

        hubAgent = DeliveryAgent.builder()
                .deliveryAgentId(agentId)
                .userId(userId)
                .slackUserId("U_HUB")
                .agentType(AgentType.HUB_DELIVERY_AGENT)
                .deliverySequence(1)
                .build();

        companyAgent = DeliveryAgent.builder()
                .deliveryAgentId(agentId)
                .userId(userId)
                .hubId(hubId)
                .slackUserId("U_COMPANY")
                .agentType(AgentType.COMPANY_DELIVERY_AGENT)
                .deliverySequence(1)
                .build();
    }


    @Test
    @DisplayName("배송 담당자 생성 - HUB 타입 정상 케이스: hubId 없이 생성된다")
    void createDeliveryAgent_hubType() {
        // given
        DeliveryAgentRequest request = mock(DeliveryAgentRequest.class);
        given(request.getUserId()).willReturn(userId);
        given(request.getSlackUserId()).willReturn("U_HUB");
        given(request.getAgentType()).willReturn(AgentType.HUB_DELIVERY_AGENT);

        given(deliveryAgentRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(false);
        given(deliveryAgentRepository.findMaxSequenceForHubType(AgentType.HUB_DELIVERY_AGENT))
                .willReturn(0);
        given(deliveryAgentRepository.saveAndFlush(any(DeliveryAgent.class))).willReturn(hubAgent);

        // when
        DeliveryAgentResponse response = deliveryAgentService.createDeliveryAgent(request);

        // then
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getAgentType()).isEqualTo(AgentType.HUB_DELIVERY_AGENT);
        verify(deliveryAgentRepository, times(1)).saveAndFlush(any(DeliveryAgent.class));
    }


    @Test
    @DisplayName("배송 담당자 생성 - COMPANY 타입 정상 케이스: 지정된 허브에 소속되어 생성된다")
    void createDeliveryAgent_companyType() {
        // given
        DeliveryAgentRequest request = mock(DeliveryAgentRequest.class);
        given(request.getUserId()).willReturn(userId);
        given(request.getHubId()).willReturn(hubId);
        given(request.getSlackUserId()).willReturn("U_COMPANY");
        given(request.getAgentType()).willReturn(AgentType.COMPANY_DELIVERY_AGENT);

        given(deliveryAgentRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(false);
        given(hubRepository.findByHubIdAndDeletedAtIsNull(hubId)).willReturn(Optional.of(hub));
        given(deliveryAgentRepository.findMaxSequenceForCompanyType(
                AgentType.COMPANY_DELIVERY_AGENT, hubId)).willReturn(0);
        given(deliveryAgentRepository.saveAndFlush(any(DeliveryAgent.class))).willReturn(companyAgent);

        // when
        DeliveryAgentResponse response = deliveryAgentService.createDeliveryAgent(request);

        // then
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getHubId()).isEqualTo(hubId);
        assertThat(response.getAgentType()).isEqualTo(AgentType.COMPANY_DELIVERY_AGENT);
    }


    @Test
    @DisplayName("배송 담당자 생성 - 동일 사용자 중복이면 DELIVERY_AGENT_ALREADY_EXISTS 예외를 던진다")
    void createDeliveryAgent_userDuplicated() {
        // given
        DeliveryAgentRequest request = mock(DeliveryAgentRequest.class);
        given(request.getUserId()).willReturn(userId);
        given(deliveryAgentRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> deliveryAgentService.createDeliveryAgent(request))
                .isInstanceOf(DeliveryAgentException.class)
                .hasFieldOrPropertyWithValue("errorCode", DeliveryAgentErrorCode.DELIVERY_AGENT_ALREADY_EXISTS);

        verify(deliveryAgentRepository, never()).saveAndFlush(any(DeliveryAgent.class));
    }


    @Test
    @DisplayName("배송 담당자 생성 - COMPANY 타입인데 hubId가 없으면 HUB_ID_REQUIRED_FOR_COMPANY_AGENT 예외를 던진다")
    void createDeliveryAgent_companyWithoutHubId() {
        // given
        DeliveryAgentRequest request = mock(DeliveryAgentRequest.class);
        given(request.getUserId()).willReturn(userId);
        given(request.getAgentType()).willReturn(AgentType.COMPANY_DELIVERY_AGENT);
        given(request.getHubId()).willReturn(null);

        given(deliveryAgentRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> deliveryAgentService.createDeliveryAgent(request))
                .isInstanceOf(DeliveryAgentException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                        DeliveryAgentErrorCode.HUB_ID_REQUIRED_FOR_COMPANY_AGENT);
    }


    @Test
    @DisplayName("배송 담당자 생성 - HUB 타입인데 hubId가 있으면 HUB_ID_NOT_ALLOWED_FOR_HUB_AGENT 예외를 던진다")
    void createDeliveryAgent_hubTypeWithHubId() {
        // given
        DeliveryAgentRequest request = mock(DeliveryAgentRequest.class);
        given(request.getUserId()).willReturn(userId);
        given(request.getAgentType()).willReturn(AgentType.HUB_DELIVERY_AGENT);
        given(request.getHubId()).willReturn(hubId);  // HUB 타입인데 hubId 지정

        given(deliveryAgentRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> deliveryAgentService.createDeliveryAgent(request))
                .isInstanceOf(DeliveryAgentException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                        DeliveryAgentErrorCode.HUB_ID_NOT_ALLOWED_FOR_HUB_AGENT);
    }


    @Test
    @DisplayName("배송 담당자 생성 - COMPANY 타입의 허브가 존재하지 않으면 HUB_NOT_FOUND 예외를 던진다")
    void createDeliveryAgent_hubNotFound() {
        // given
        DeliveryAgentRequest request = mock(DeliveryAgentRequest.class);
        given(request.getUserId()).willReturn(userId);
        given(request.getAgentType()).willReturn(AgentType.COMPANY_DELIVERY_AGENT);
        given(request.getHubId()).willReturn(hubId);

        given(deliveryAgentRepository.existsByUserIdAndDeletedAtIsNull(userId)).willReturn(false);
        given(hubRepository.findByHubIdAndDeletedAtIsNull(hubId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryAgentService.createDeliveryAgent(request))
                .isInstanceOf(HubException.class)
                .hasFieldOrPropertyWithValue("errorCode", HubErrorCode.HUB_NOT_FOUND);
    }


    @Test
    @DisplayName("배송 담당자 수정 - slackUserId만 변경되는 정상 케이스")
    void updateDeliveryAgent_slackOnly() {
        // given
        DeliveryAgentUpdateRequest request = mock(DeliveryAgentUpdateRequest.class);
        given(request.getHubId()).willReturn(null);
        given(request.getSlackUserId()).willReturn("U_NEW_SLACK");

        given(deliveryAgentRepository.findByDeliveryAgentIdAndDeletedAtIsNull(agentId))
                .willReturn(Optional.of(hubAgent));
        given(deliveryAgentRepository.saveAndFlush(hubAgent)).willReturn(hubAgent);

        // when
        DeliveryAgentResponse response = deliveryAgentService.updateDeliveryAgent(agentId, request);

        // then
        assertThat(response.getSlackUserId()).isEqualTo("U_NEW_SLACK");
        verify(deliveryAgentRepository, times(1)).saveAndFlush(hubAgent);
    }

    @Test
    @DisplayName("배송 담당자 수정 - 존재하지 않으면 DELIVERY_AGENT_NOT_FOUND 예외를 던진다")
    void updateDeliveryAgent_notFound() {
        // given
        DeliveryAgentUpdateRequest request = mock(DeliveryAgentUpdateRequest.class);
        given(deliveryAgentRepository.findByDeliveryAgentIdAndDeletedAtIsNull(agentId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryAgentService.updateDeliveryAgent(agentId, request))
                .isInstanceOf(DeliveryAgentException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                        DeliveryAgentErrorCode.DELIVERY_AGENT_NOT_FOUND);
    }


    @Test
    @DisplayName("배송 담당자 수정 - HUB 타입이 hubId 변경을 시도하면 HUB_ID_NOT_ALLOWED_FOR_HUB_AGENT 예외를 던진다")
    void updateDeliveryAgent_hubTypeCannotChangeHubId() {
        // given
        UUID newHubId = UUID.randomUUID();
        DeliveryAgentUpdateRequest request = mock(DeliveryAgentUpdateRequest.class);
        given(request.getHubId()).willReturn(newHubId);

        given(deliveryAgentRepository.findByDeliveryAgentIdAndDeletedAtIsNull(agentId))
                .willReturn(Optional.of(hubAgent));  // HUB 타입

        // when & then
        assertThatThrownBy(() -> deliveryAgentService.updateDeliveryAgent(agentId, request))
                .isInstanceOf(DeliveryAgentException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                        DeliveryAgentErrorCode.HUB_ID_NOT_ALLOWED_FOR_HUB_AGENT);
    }


    @Test
    @DisplayName("배송 담당자 수정 - COMPANY 타입의 새 허브가 존재하지 않으면 HUB_NOT_FOUND 예외를 던진다")
    void updateDeliveryAgent_newHubNotFound() {
        // given
        UUID newHubId = UUID.randomUUID();
        DeliveryAgentUpdateRequest request = mock(DeliveryAgentUpdateRequest.class);
        given(request.getHubId()).willReturn(newHubId);

        given(deliveryAgentRepository.findByDeliveryAgentIdAndDeletedAtIsNull(agentId))
                .willReturn(Optional.of(companyAgent));
        given(hubRepository.findByHubIdAndDeletedAtIsNull(newHubId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryAgentService.updateDeliveryAgent(agentId, request))
                .isInstanceOf(HubException.class)
                .hasFieldOrPropertyWithValue("errorCode", HubErrorCode.HUB_NOT_FOUND);
    }


    @Test
    @DisplayName("배송 담당자 삭제 - 정상 케이스: 소프트 삭제 처리되고 deletedAt/deletedBy가 기록된다")
    void deleteDeliveryAgent() {
        // given
        UUID adminId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(adminId, UserRole.ROLE_MASTER, null, null);

        given(deliveryAgentRepository.findByDeliveryAgentIdAndDeletedAtIsNull(agentId))
                .willReturn(Optional.of(hubAgent));
        given(deliveryRepository.existsByDeliveryAgentIdAndDeliveryStatusNotInAndDeletedAtIsNull(
                eq(agentId),
                anyList()
        )).willReturn(false);

        // when
        DeliveryAgentResponse response = deliveryAgentService.deleteDeliveryAgent(agentId, principal);

        // then
        assertThat(response.getDeliveryAgentId()).isEqualTo(agentId);
        assertThat(hubAgent.getDeletedAt()).isNotNull();
        assertThat(hubAgent.getDeletedBy()).isEqualTo(adminId);
    }


    @Test
    @DisplayName("배송 담당자 삭제 - 진행 중 배송이 있으면 AGENT_HAS_ONGOING_DELIVERY 예외를 던진다")
    void deleteDeliveryAgent_hasOngoingDelivery() {
        // given
        UUID adminId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(adminId, UserRole.ROLE_MASTER, null, null);

        given(deliveryAgentRepository.findByDeliveryAgentIdAndDeletedAtIsNull(agentId))
                .willReturn(Optional.of(hubAgent));
        given(deliveryRepository.existsByDeliveryAgentIdAndDeliveryStatusNotInAndDeletedAtIsNull(
                eq(agentId),
                anyList()
        )).willReturn(true);  // 진행 중 배송 있음

        // when & then
        assertThatThrownBy(() -> deliveryAgentService.deleteDeliveryAgent(agentId, principal))
                .isInstanceOf(DeliveryAgentException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                        DeliveryAgentErrorCode.AGENT_HAS_ONGOING_DELIVERY);

        // 소프트 삭제가 호출되지 않았는지 (deletedAt 미설정)
        assertThat(hubAgent.getDeletedAt()).isNull();
    }


    @Test
    @DisplayName("배송 담당자 삭제 - 존재하지 않으면 DELIVERY_AGENT_NOT_FOUND 예외를 던진다")
    void deleteDeliveryAgent_notFound() {
        // given
        UUID adminId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(adminId, UserRole.ROLE_MASTER, null, null);

        given(deliveryAgentRepository.findByDeliveryAgentIdAndDeletedAtIsNull(agentId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryAgentService.deleteDeliveryAgent(agentId, principal))
                .isInstanceOf(DeliveryAgentException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                        DeliveryAgentErrorCode.DELIVERY_AGENT_NOT_FOUND);
    }


    @Test
    @DisplayName("배송 담당자 단건 조회 - 정상 케이스")
    void getDeliveryAgent() {
        // given
        given(deliveryAgentRepository.findByDeliveryAgentIdAndDeletedAtIsNull(agentId))
                .willReturn(Optional.of(hubAgent));

        // when
        DeliveryAgentResponse response = deliveryAgentService.getDeliveryAgent(agentId);

        // then
        assertThat(response.getDeliveryAgentId()).isEqualTo(agentId);
        assertThat(response.getUserId()).isEqualTo(userId);
    }


    @Test
    @DisplayName("배송 담당자 단건 조회 - 존재하지 않으면 DELIVERY_AGENT_NOT_FOUND 예외를 던진다")
    void getDeliveryAgent_notFound() {
        // given
        given(deliveryAgentRepository.findByDeliveryAgentIdAndDeletedAtIsNull(agentId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryAgentService.getDeliveryAgent(agentId))
                .isInstanceOf(DeliveryAgentException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                        DeliveryAgentErrorCode.DELIVERY_AGENT_NOT_FOUND);
    }


    @Test
    @DisplayName("배송 담당자 검색 - 조건 기반 페이징 결과를 반환한다")
    void searchDeliveryAgents() {
        // given
        DeliveryAgentSearchRequest request = new DeliveryAgentSearchRequest(
                hubId, AgentType.HUB_DELIVERY_AGENT, "U_HUB");
        Pageable pageable = PageRequest.of(0, 10);
        Page<DeliveryAgent> agentPage = new PageImpl<>(List.of(hubAgent), pageable, 1);

        given(deliveryAgentRepository.searchDeliveryAgents(
                hubId, AgentType.HUB_DELIVERY_AGENT, "U_HUB", pageable
        )).willReturn(agentPage);

        // when
        Page<DeliveryAgentResponse> result =
                deliveryAgentService.searchDeliveryAgents(request, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDeliveryAgentId()).isEqualTo(agentId);
    }
}