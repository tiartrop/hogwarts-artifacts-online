package edu.tcu.cs.hogwarts_artifacts_online.system.actuator;

import java.io.File;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

@Component
public class UsableMemoryHealthIndicator implements HealthIndicator {

  @Override
  public Health health() {
    File path = new File("."); // Path used to compute available disk space.
    long diskUsableInBytes = path.getUsableSpace();
    boolean isHealth = diskUsableInBytes >= 10 * 1024 * 1024; // 10MB
    Status status = isHealth ? Status.UP : Status.DOWN; // UP means there is enough usable memory.
    return Health.status(status).withDetail("usable memory", diskUsableInBytes).withDetail("threshold", 10 * 1024 * 1024).build();
  }

}
