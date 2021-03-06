package org.fatecrafters.plugins.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.fatecrafters.plugins.RealisticBackpacks;

public class MysqlFunctions {

	private static RealisticBackpacks plugin;

	public static void setMysqlFunc(final RealisticBackpacks plugin) {
		MysqlFunctions.plugin = plugin;
	}

	public static boolean checkIfTableExists(final String table) {
		try {
			final Connection conn = DriverManager.getConnection(plugin.getUrl(), plugin.getUser(), plugin.getPass());
			final Statement state = conn.createStatement();
			final DatabaseMetaData dbm = conn.getMetaData();
			final ResultSet tables = dbm.getTables(null, null, "rb_data", null);
			state.close();
			conn.close();
			if (tables.next()) {
				return true;
			} else {
				return false;
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void createTables() {
		try {
			final Connection conn = DriverManager.getConnection(plugin.getUrl(), plugin.getUser(), plugin.getPass());
			final PreparedStatement state = conn.prepareStatement("CREATE TABLE rb_data (player VARCHAR(16), backpack VARCHAR(20), inventory TEXT);");
			state.executeUpdate();
			state.close();
			conn.close();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	public static void addBackpackData(final String playerName, final String backpack, final List<String> invString) throws SQLException {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					final Connection conn = DriverManager.getConnection(plugin.getUrl(), plugin.getUser(), plugin.getPass());
					PreparedStatement statement = conn.prepareStatement("SELECT EXISTS(SELECT 1 FROM rb_data WHERE player = ? AND backpack = ? LIMIT 1);");
					statement.setString(1, playerName);
					statement.setString(2, backpack);
					final ResultSet res = statement.executeQuery();
					PreparedStatement state = null;
					if (res.next()) {
						if (res.getInt(1) == 1) {
							state = conn.prepareStatement("UPDATE rb_data SET player=?, backpack=?, inventory=? WHERE player=? AND backpack=?;");
							state.setString(1, playerName);
							state.setString(2, backpack);
							state.setString(3, Serialization.listToString(invString));
							state.setString(4, playerName);
							state.setString(5, backpack);
						} else {
							state = conn.prepareStatement("INSERT INTO rb_data (player, backpack, inventory) VALUES(?, ?, ?);");
							state.setString(1, playerName);
							state.setString(2, backpack);
							state.setString(3, Serialization.listToString(invString));
						}
					}
					state.executeUpdate();
					state.close();
					conn.close();
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static Inventory getBackpackInv(final String playerName, final String backpack) throws SQLException {
		Inventory returnInv = null;
		try {
			final Connection conn = DriverManager.getConnection(plugin.getUrl(), plugin.getUser(), plugin.getPass());
			final PreparedStatement state = conn.prepareStatement("SELECT inventory FROM rb_data WHERE player=? AND backpack=? LIMIT 1;");
			state.setString(1, playerName);
			state.setString(2, backpack);
			final ResultSet res = state.executeQuery();
			if (res.next()) {
				final String invString = res.getString(1);
				if (invString != null) {
					returnInv = Serialization.toInventory(Serialization.stringToList(invString), ChatColor.translateAlternateColorCodes('&', plugin.backpackData.get(backpack).get(3)), Integer.parseInt(plugin.backpackData.get(backpack).get(0)));
				} else {
					returnInv = null;
				}
			}
			state.close();
			conn.close();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return returnInv;
	}

	public static void delete(final String playerName, final String backpack) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					final Connection conn = DriverManager.getConnection(plugin.getUrl(), plugin.getUser(), plugin.getPass());
					final PreparedStatement state = conn.prepareStatement("DELETE FROM rb_data WHERE player = ? AND backpack = ?;");
					state.setString(1, playerName);
					state.setString(2, backpack);
					state.executeUpdate();
					state.close();
					conn.close();
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
