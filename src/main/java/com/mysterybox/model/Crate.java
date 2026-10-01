package com.mysterybox.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Crate {

    private final String id;
    private final String name;
    private final CrateKey key;
    private final List<CrateReward> rewards;

    public Crate(String id, String name, CrateKey key, List<CrateReward> rewards) {
        this.id = id;
        this.name = name != null ? name : id;
        this.key = key;
        this.rewards = rewards != null ? rewards : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CrateKey getKey() {
        return key;
    }

    public List<CrateReward> getRewards() {
        return rewards;
    }

    public double getTotalChance() {
        double total = 0.0;
        for (CrateReward reward : rewards) {
            total += Math.max(0.0, reward.getChance());
        }
        return total;
    }

    public CrateReward chooseRandomReward() {
        if (rewards.isEmpty()) {
            return null;
        }

        double totalChance = getTotalChance();
        if (totalChance <= 0.0) {
            return rewards.get(ThreadLocalRandom.current().nextInt(rewards.size()));
        }

        double randomValue = ThreadLocalRandom.current().nextDouble() * totalChance;
        double cumulative = 0.0;

        for (CrateReward reward : rewards) {
            cumulative += Math.max(0.0, reward.getChance());
            if (randomValue <= cumulative) {
                return reward;
            }
        }

        return rewards.get(rewards.size() - 1);
    }
}
