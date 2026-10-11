package com.axis.nachimbal.domain.route.repository;

import com.axis.nachimbal.domain.route.entity.RouteFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface RouteFavoriteRepository extends JpaRepository<RouteFavorite, Long> {

    boolean existsByUserIdAndRouteId(Long userId, Long routeId);

    void deleteByUserIdAndRouteId(Long userId, Long routeId);

    void deleteByRouteId(Long routeId);

    @Query("select f.routeId from RouteFavorite f where f.userId = :userId")
    Set<Long> findRouteIdsByUserId(@Param("userId") Long userId);
}
