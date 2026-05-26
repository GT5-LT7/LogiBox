package com.sparta.gt5lt7.logisticsservice.infrastructure.seed;

import com.sparta.gt5lt7.logisticsservice.application.HubRouteService;
import com.sparta.gt5lt7.logisticsservice.domain.entity.Hub;
import com.sparta.gt5lt7.logisticsservice.domain.entity.HubRoute;
import com.sparta.gt5lt7.logisticsservice.domain.repository.HubRepository;
import com.sparta.gt5lt7.logisticsservice.domain.repository.HubRouteRepository;
import com.sparta.gt5lt7.logisticsservice.infrastructure.client.kakao.KakaoMobilityClient;
import com.sparta.gt5lt7.logisticsservice.infrastructure.client.kakao.dto.KakaoDirectionsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 로컬/개발 환경에서 17개 허브의 모든 페어(17 × 16 = 272개) HubRoute를 카카오모빌리티 길찾기 API로 자동 생성한다.
 시드 완료 후 모든 경로를 한 번씩 조회해 Redis 캐시에 미리 적재(warm-up)한다.
 멱등성: 이미 존재하는 (fromHubId, toHubId) 페어는 스킵.
 QPS 제어: 호출 간 {@link #API_CALL_INTERVAL_MS} ms 대기.
 */
@Slf4j
@Configuration
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class HubRouteSeeder {

    private final HubRepository hubRepository;
    private final HubRouteRepository hubRouteRepository;
    private final HubRouteService hubRouteService;
    private final KakaoMobilityClient kakaoMobilityClient;

    private static final long API_CALL_INTERVAL_MS = 100;

    @Bean
    @Order(2)
    ApplicationRunner seedHubRoutes() {
        return args -> {
            seed();
            if (Thread.currentThread().isInterrupted()) {
                log.warn("[HubRouteSeeder] 인터럽트 상태 감지, 캐시 워밍업 스킵");
                return;
            }
            warmCache();
        };
    }

    @Transactional
    public void seed() {
        List<Hub> hubs = hubRepository.findAll().stream()
                .filter(h -> h.getDeletedAt() == null)
                .toList();

        if (hubs.size() < 2) {
            log.warn("[HubRouteSeeder] 활성 허브가 2개 미만입니다. 시드 스킵.");
            return;
        }

        int created = 0;
        int skipped = 0;
        int failed = 0;

        for (Hub from : hubs) {
            for (Hub to : hubs) {
                if (from.getHubId().equals(to.getHubId())) {
                    continue;
                }

                if (hubRouteRepository.existsByFromHubIdAndToHubIdAndDeletedAtIsNull(
                        from.getHubId(), to.getHubId())) {
                    skipped++;
                    continue;
                }

                try {
                    String origin = from.getLongitude() + "," + from.getLatitude();
                    String destination = to.getLongitude() + "," + to.getLatitude();
                    KakaoDirectionsResponse response =
                            kakaoMobilityClient.getDirections(origin, destination);

                    if (response.isEmpty()) {
                        log.warn("[HubRouteSeeder] 경로 없음: {} → {}", from.getName(), to.getName());
                        failed++;
                        continue;
                    }

                    KakaoDirectionsResponse.Summary summary = response.firstSummary();
                    int distanceKm = Math.max(1, summary.getDistance() / 1000);
                    int durationMin = Math.max(1, summary.getDuration() / 60);

                    hubRouteRepository.save(
                            HubRoute.create(from.getHubId(), to.getHubId(), distanceKm, durationMin)
                    );
                    created++;

                    log.debug("[HubRouteSeeder] 생성: {} → {} ({}km, {}min)",
                            from.getName(), to.getName(), distanceKm, durationMin);

                    Thread.sleep(API_CALL_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("[HubRouteSeeder] 인터럽트 발생, 시드 중단", ie);
                    return;
                } catch (Exception e) {
                    log.error("[HubRouteSeeder] 경로 시드 실패: {} → {}",
                            from.getName(), to.getName(), e);
                    failed++;
                }
            }
        }

        log.info("[HubRouteSeeder] 시드 완료: 생성={}, 스킵={}, 실패={}", created, skipped, failed);
    }

    /*
     캐시 워밍업: 모든 활성 HubRoute를 {@link HubRouteService#getHubRouteByHubs} 통해 한 번씩 호출.
     주의: {@code @Cacheable} 은 프록시 기반이라 반드시 외부(다른 Bean)에서 호출해야 동작한다.
     본 메서드는 시더가 서비스를 호출하므로 안전.
     */
    public void warmCache() {
        List<HubRoute> activeRoutes = hubRouteRepository.findAll().stream()
                .filter(r -> r.getDeletedAt() == null)
                .toList();

        int warmed = 0;
        for (HubRoute r : activeRoutes) {
            try {
                hubRouteService.getHubRouteByHubs(r.getFromHubId(), r.getToHubId());
                warmed++;
            } catch (Exception e) {
                log.warn("[HubRouteSeeder] 캐시 워밍 실패: routeId={}", r.getRouteId(), e);
            }
        }
        log.info("[HubRouteSeeder] 캐시 워밍업 완료: {}/{}건", warmed, activeRoutes.size());
    }
}
