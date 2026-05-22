package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.sparta.gt5lt7.logisticsservice.domain.entity.Hub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HubRepository extends JpaRepository<Hub, UUID> {

    /**
 * Finds an active Hub by its hubId (only hubs with deletedAt equal to null).
 *
 * @param hubId the UUID identifier of the hub to find
 * @return an Optional containing the matching Hub if present, or empty if no active hub with the given id exists
 */
Optional<Hub> findByHubIdAndDeletedAtIsNull(UUID hubId);

    /**
 * Determines whether an active Hub with the given name exists.
 *
 * @param name the Hub name to check
 * @return {@code true} if at least one Hub exists with the given name and a null {@code deletedAt}, {@code false} otherwise
 */
boolean existsByNameAndDeletedAtIsNull(String name);
}