package com.plotsquared.bukkit.titles;

import com.plotsquared.bukkit.chat.Reflection;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DefaultTitleManager_183 extends TitleManager {

    /**
     * Create a new 1.8 title.
     *
     * @param title Title text
     * @param subtitle Subtitle text
     * @param fadeInTime Fade in time
     * @param stayTime Stay on screen time
     * @param fadeOutTime Fade out time
     */
    public DefaultTitleManager_183(String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime) {
        super(title, subtitle, fadeInTime, stayTime, fadeOutTime);
    }

    private static boolean equalsTypeArray(Class<?>[] a, Class<?>[] o) {
        if (a.length != o.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(o[i]) && !a[i].isAssignableFrom(o[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Load spigot and NMS classes.
     */
    @Override
    void loadClasses() {
        this.packetTitle = Reflection.getNMSClass("PacketPlayOutTitle");
        this.chatBaseComponent = Reflection.getNMSClass("IChatBaseComponent");
        this.packetActions = Reflection.getNMSClass("PacketPlayOutTitle$EnumTitleAction");
        this.nmsChatSerializer = Reflection.getNMSClass("IChatBaseComponent$ChatSerializer");

    }

    @Override
    public void send(Player player) throws Exception {
        if (this.packetTitle != null) {
            // First reset previous settings
            resetTitle(player);
            // Send timings first
            Object handle = getHandle(player);
            Object connection = getField(handle.getClass(), "playerConnection").get(handle);
            Object[] actions = this.packetActions.getEnumConstants();
            Method sendPacket = getMethod(connection.getClass(), "sendPacket");
            Object packet = this.packetTitle
                    .getConstructor(this.packetActions, this.chatBaseComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE)
                    .newInstance(actions[2], null,
                            this.fadeInTime * (this.ticks ? 1 : 20),
                            this.stayTime * (this.ticks ? 1 : 20), this.fadeOutTime * (this.ticks ? 1 : 20));
            // Send if set
            if ((this.fadeInTime != -1) && (this.fadeOutTime != -1) && (this.stayTime != -1)) {
                sendPacket.invoke(connection, packet);
            }
            // Send title
            Object serialized = getMethod(this.nmsChatSerializer, "a", String.class).invoke(null,
                    "{text:\"" + ChatColor.translateAlternateColorCodes('&', this.getTitle()) + "\",color:" + this.titleColor.name().toLowerCase()
                            + "}");
            packet = this.packetTitle.getConstructor(this.packetActions, this.chatBaseComponent).newInstance(actions[0], serialized);
            sendPacket.invoke(connection, packet);
            if (!this.getSubtitle().isEmpty()) {
                // Send subtitle if present
                serialized = getMethod(this.nmsChatSerializer, "a", String.class).invoke(null,
                        "{text:\"" + ChatColor.translateAlternateColorCodes('&', this.getSubtitle()) + "\",color:" + this.subtitleColor.name()
                                .toLowerCase() + "}");
                packet = this.packetTitle.getConstructor(this.packetActions, this.chatBaseComponent).newInstance(actions[1], serialized);
                sendPacket.invoke(connection, packet);
            }
        }
    }

    /**
     * Clear the title.
     *
     * @param player Player
     * @throws Exception
     */
    @Override
    public void clearTitle(Player player) throws Exception {
        // Send timings first
        Object handle = getHandle(player);
        Object connection = getField(handle.getClass(), "playerConnection").get(handle);
        Object[] actions = this.packetActions.getEnumConstants();
        Method sendPacket = getMethod(connection.getClass(), "sendPacket");
        Object packet = this.packetTitle.getConstructor(this.packetActions, this.chatBaseComponent).newInstance(actions[3], null);
        sendPacket.invoke(connection, packet);
    }

    /**
     * Reset the title settings.
     *
     * @param player Player
     * @throws Exception
     */
    @Override
    public void resetTitle(Player player) throws Exception {
        // Send timings first
        Object handle = getHandle(player);
        Object connection = getField(handle.getClass(), "playerConnection").get(handle);
        Object[] actions = this.packetActions.getEnumConstants();
        Method sendPacket = getMethod(connection.getClass(), "sendPacket");
        Object packet = this.packetTitle.getConstructor(this.packetActions, this.chatBaseComponent).newInstance(actions[4], null);
        sendPacket.invoke(connection, packet);
    }

    private Object getHandle(Object obj) {
        try {
            return getMethod("getHandle", obj.getClass()).invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Method getMethod(String name, Class<?> clazz, Class<?>... paramTypes) {
        Class<?>[] t = toPrimitiveTypeArray(paramTypes);
        for (Method m : clazz.getMethods()) {
            Class<?>[] types = toPrimitiveTypeArray(m.getParameterTypes());
            if (m.getName().equals(name) && equalsTypeArray(types, t)) {
                return m;
            }
        }
        return null;
    }

    private Field getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Method getMethod(Class<?> clazz, String name, Class<?>... args) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && ((args.length == 0) || ClassListEqual(args, m.getParameterTypes()))) {
                m.setAccessible(true);
                return m;
            }
        }
        return null;
    }

    private boolean ClassListEqual(Class<?>[] l1, Class<?>[] l2) {
        if (l1.length != l2.length) {
            return false;
        }
        boolean equal = true;
        for (int i = 0; i < l1.length; i++) {
            if (l1[i] != l2[i]) {
                equal = false;
                break;
            }
        }
        return equal;
    }
}
