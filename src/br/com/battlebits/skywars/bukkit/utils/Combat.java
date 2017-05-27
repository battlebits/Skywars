package br.com.battlebits.skywars.bukkit.utils;

import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;

public class Combat {

    @Setter
    @Getter
    private Player damager;

    private long expire = 0;

    public boolean isValid() {
        return expire > System.currentTimeMillis();
    }

    public void setExpire(long time) {
        this.expire = System.currentTimeMillis() + time;
    }
}
