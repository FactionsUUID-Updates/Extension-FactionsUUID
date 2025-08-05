package net.playeranalytics.extension.factionsuuid;

import com.djrapitops.plan.extension.Group;
import com.djrapitops.plan.extension.NotReadyException;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.perms.Relation;
import com.massivecraft.factions.perms.Role;
import com.massivecraft.factions.util.TL;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public class FactionsUUIDExtProviderLegacy implements FactionsUUIDExtProvider {
    private final Method getTranslation;

    public FactionsUUIDExtProviderLegacy() {
        try {
            Method methodGetTranslation = Role.class.getMethod("getTranslation");
            if (String.class.equals(methodGetTranslation.getReturnType())) { // FUUID 0.5.25+
                this.getTranslation = null;
            } else { // FUUID prior to 0.5.25, and forks.
                this.getTranslation = methodGetTranslation;
            }
        } catch (NoSuchMethodException e) { // What? How?
            throw new IllegalStateException("Found an unsupported FactionsUUID variant", e);
        }
    }

    private FPlayer getFPlayer(UUID playerUUID) {
        // This shouldn't ever fail
        // due to *questionable* design, it will generate a new entry if the uuid isn't found in memory
        // If this is accessed before FactionsUUID is loaded it will generate
        // a 'grumpy exception' that we cannot handle, but will still run fine
        return FPlayers.getInstance().getById(playerUUID.toString());
    }

    private Optional<Faction> getFaction(UUID playerUUID) {
        FPlayer fPlayer = getFPlayer(playerUUID);

        Faction faction = fPlayer.getFaction();
        if (!faction.isNormal()) return Optional.empty(); // ignore wilderness, safezone & warzone

        return Optional.of(faction);
    }

    private Faction getFaction(Group factionName) {
        return Factions.getInstance().getByTag(factionName.getGroupName());
    }

    @Override
    public String[] factionName(UUID playerUUID) {
        return this.getFaction(playerUUID)
                .map(faction -> new String[]{faction.getTag()})
                .orElse(new String[0]);
    }

    @Override
    public boolean hasFaction(UUID playerUUID) {
        return this.getFaction(playerUUID).isPresent();
    }

    @Override
    public double power(UUID playerUUID) {
        return getFPlayer(playerUUID).getPower();
    }

    @Override
    public double powerMax(UUID playerUUID) {
        return getFPlayer(playerUUID).getPowerMax();
    }

    @Override
    public String role(UUID playerUUID) {
        if (getTranslation == null) { // FUUID 0.5.25+
            return getFPlayer(playerUUID)
                    .getRole().getTranslation();
        } else {
            try {
                return ((TL) getTranslation.invoke(getFPlayer(playerUUID))).format(); // FUUID prior to 0.5.25, and forks.
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                throw new NotReadyException();
            }
        }
    }

    @Override
    public boolean economyEnabled(Group factionName) {
        return Econ.shouldBeUsed() && (getTranslation != null || FactionsPlugin.getInstance().conf().economy().isBankEnabled());
    }

    @Override
    public String factionLeader(Group factionName) {
        Faction faction = getFaction(factionName);
        return faction.getFPlayers().stream().
                filter(fPlayer -> fPlayer.getRole() == Role.ADMIN)
                .findAny().map(FPlayer::getName)
                .orElseThrow(() -> new IllegalStateException("Faction doesn't have a leader"));
    }

    @Override
    public String description(Group factionName) {
        Faction faction = getFaction(factionName);
        return faction.getDescription();
    }

    @Override
    public double power(Group factionName) {
        Faction faction = getFaction(factionName);
        return faction.getPower();
    }

    @Override
    public double maxPower(Group factionName) {
        Faction faction = getFaction(factionName);
        return faction.getPowerMax();
    }

    @Override
    public long created(Group factionName) {
        Faction faction = getFaction(factionName);
        return faction.getFoundedDate();
    }

    @Override
    public long memberCount(Group factionName) {
        Faction faction = getFaction(factionName);
        return faction.getSize();
    }

    @Override
    public long allyCount(Group factionName) {
        Faction faction = getFaction(factionName);
        return faction.getRelationCount(Relation.ALLY);
    }

    @Override
    public long truceCount(Group factionName) {
        Faction faction = getFaction(factionName);
        return faction.getRelationCount(Relation.TRUCE);
    }

    @Override
    public double balance(Group factionName) {
        Faction faction = getFaction(factionName);
        return Econ.getBalance(faction.getAccountId());
    }
}
