package br.com.battlebits.skywars.game.kits.classes;

import br.com.battlebits.skywars.game.kits.Kit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Arquivo criado em 29/03/2017.
 * Desenvolvido por:
 *
 * @author Luãn Pereira.
 */
public class Archer extends Kit
{
    public Archer()
    {
        setName("Archer");
        setIcon(new ItemStack(Material.BOW));
    }
}
