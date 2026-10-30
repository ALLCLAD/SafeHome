package com.safehome.repository;

import com.safehome.model.Device;
import com.safehome.model.enums.DeviceType;
import com.safehome.model.enums.ZoneType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByZone(ZoneType zone);
    List<Device> findByType(DeviceType type);
    List<Device> findByZoneAndType(ZoneType zone, DeviceType type);
}
