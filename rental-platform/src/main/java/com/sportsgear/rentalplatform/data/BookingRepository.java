package com.sportsgear.rentalplatform.data; // Nota: Se mudaste para .repository, ajusta aqui

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // History
    List<Booking> findByRenterId(Long renterId);

    @Query("SELECT COUNT(b) > 0 FROM Booking b "
            + "JOIN b.items bi "
            + "WHERE bi.item.id IN :itemIds "
            + "AND b.status <> 'CANCELLED' "
            + "AND (b.endDate > :startDate AND b.startDate < :endDate)")
    boolean existsOverlappingBookings(
            @Param("itemIds") List<Long> itemIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
