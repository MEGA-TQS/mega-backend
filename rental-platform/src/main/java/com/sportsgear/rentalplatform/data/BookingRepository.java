package com.sportsgear.rentalplatform.data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // History
    List<Booking> findByRenterId(Long renterId);
    
    // Used to check availability overlapping
    List<Booking> findByItems_Item_IdAndEndDateAfterAndStartDateBefore(
        Long itemId, LocalDate start, LocalDate end
    );
}
