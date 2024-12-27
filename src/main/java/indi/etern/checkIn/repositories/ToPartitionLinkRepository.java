package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToPartitionLinkRepository extends JpaRepository<ToPartitionsLink,String> {
}