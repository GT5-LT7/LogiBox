package com.sparta.gt5lt7.logisticsservice.infrastructure.seed;

import com.sparta.gt5lt7.logisticsservice.domain.entity.Hub;
import com.sparta.gt5lt7.logisticsservice.domain.repository.HubRepository;
import com.sparta.gt5lt7.logisticsservice.infrastructure.client.kakao.KakaoLocalClient;
import com.sparta.gt5lt7.logisticsservice.infrastructure.client.kakao.dto.KakaoLocalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class HubDataSeeder {

    private final HubRepository hubRepository;
    private final KakaoLocalClient kakaoLocalClient;

    // (이름, 주소) — 좌표는 카카오 API로 자동 변환
    private static final List<HubSeed> HUB_SEEDS = List.of(
            new HubSeed("서울특별시 센터", "서울특별시 송파구 송파대로 55"),
            new HubSeed("경기 북부 센터", "경기도 고양시 덕양구 권율대로 570"),
            new HubSeed("경기 남부 센터", "경기도 이천시 덕평로 257-21"),
            new HubSeed("부산광역시 센터", "부산 동구 중앙대로 206"),
            new HubSeed("대구광역시 센터", "대구 북구 태평로 161"),
            new HubSeed("인천광역시 센터", "인천 남동구 정각로 29"),
            new HubSeed("광주광역시 센터", "광주 서구 내방로 111"),
            new HubSeed("대전광역시 센터", "대전 서구 둔산로 100"),
            new HubSeed("울산광역시 센터", "울산 남구 중앙로 201"),
            new HubSeed("세종특별자치시 센터", "세종특별자치시 한누리대로 2130"),
            new HubSeed("강원특별자치도 센터", "강원특별자치도 춘천시 중앙로 1"),
            new HubSeed("충청북도 센터", "충북 청주시 상당구 상당로 82"),
            new HubSeed("충청남도 센터", "충남 홍성군 홍북읍 충남대로 21"),
            new HubSeed("전북특별자치도 센터", "전북특별자치도 전주시 완산구 효자로 225"),
            new HubSeed("전라남도 센터", "전남 무안군 삼향읍 오룡길 1"),
            new HubSeed("경상북도 센터", "경북 안동시 풍천면 도청대로 455"),
            new HubSeed("경상남도 센터", "경남 창원시 의창구 중앙대로 300")
    );

    @Bean
    @Order(1)
    ApplicationRunner seedHubs() {
        return args -> seed();
    }

    @Transactional
    public void seed() {
        List<Hub> hubs = new ArrayList<>(HUB_SEEDS.size());
        for (HubSeed s : HUB_SEEDS) {
            if (hubRepository.existsByNameAndDeletedAtIsNull(s.name())) {
                log.info("[HubSeeder] 이미 존재하는 허브 스킵: {}", s.name());
                continue;
            }
            Coordinate coord = geocode(s.address());
            hubs.add(Hub.create(s.name(), s.address(), coord.latitude(), coord.longitude()));
            log.info("[HubSeeder] 좌표 변환 완료: {} → ({}, {})", s.name(), coord.latitude(), coord.longitude());
        }

        if (hubs.isEmpty()) {
            log.info("[HubSeeder] 모든 허브 데이터가 이미 존재하여 시드를 스킵합니다.");
            return;
        }

        hubRepository.saveAll(hubs);
        log.info("[HubSeeder] {}개 허브 시드 완료", hubs.size());
    }

    private Coordinate geocode(String address) {
        try {
            KakaoLocalResponse response = kakaoLocalClient.searchAddress(address);
            if (response.isEmpty()) {
                throw new IllegalStateException("카카오 좌표 변환 실패 (검색 결과 없음): " + address);
            }
            KakaoLocalResponse.Document doc = response.first();
            return new Coordinate(doc.getLatitude(), doc.getLongitude());
        } catch (Exception e) {
            log.error("[HubSeeder] 카카오 API 호출 실패: address={}", address, e);
            throw new IllegalStateException("좌표 변환 중 오류 발생: " + address, e);
        }
    }

    private record HubSeed(String name, String address) {}
    private record Coordinate(double latitude, double longitude) {}
}