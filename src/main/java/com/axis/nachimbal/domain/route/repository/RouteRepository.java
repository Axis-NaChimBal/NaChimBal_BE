package com.axis.nachimbal.domain.route.repository;

import com.axis.nachimbal.domain.route.entity.Route;
import com.axis.nachimbal.domain.route.entity.RouteCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findByUserIdAndCategoryOrderByCreatedAtDesc(Long userId, RouteCategory category);

    List<Route> findByCategoryOrderByCreatedAtDesc(RouteCategory category);

    Optional<Route> findByIdAndUserId(Long id, Long userId);
}
