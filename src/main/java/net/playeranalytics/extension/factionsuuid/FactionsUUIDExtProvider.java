package net.playeranalytics.extension.factionsuuid;

import com.djrapitops.plan.extension.Group;

import java.util.UUID;

public interface FactionsUUIDExtProvider {
    String[] factionName(UUID playerUUID);

    boolean hasFaction(UUID playerUUID);

    double power(UUID playerUUID);

    double powerMax(UUID playerUUID);

    String role(UUID playerUUID);

    boolean economyEnabled(Group factionName);

    String factionLeader(Group factionName);

    String description(Group factionName);

    double power(Group factionName);

    double maxPower(Group factionName);

    long created(Group factionName);

    long memberCount(Group factionName);

    long allyCount(Group factionName);

    long truceCount(Group factionName);

    double balance(Group factionName);
}
