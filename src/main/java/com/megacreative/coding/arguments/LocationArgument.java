package com.megacreative.coding.arguments;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.LocationValue;
import com.megacreative.coding.values.Value;
import java.util.Optional;

/**
 * Аргумент для извлечения локации из параметров блока.
 * Поддерживает формат "x y z" или "x y z world".
 */
public class LocationArgument implements Argument<LocationValue> {
    private final String xParameter;
    private final String yParameter;
    private final String zParameter;
    private final String worldParameter;

    public LocationArgument(String xParameter, String yParameter, String zParameter) {
        this(xParameter, yParameter, zParameter, null);
    }

    public LocationArgument(String xParameter, String yParameter, String zParameter, String worldParameter) {
        this.xParameter = xParameter;
        this.yParameter = yParameter;
        this.zParameter = zParameter;
        this.worldParameter = worldParameter;
    }

    @Override
    public Optional<LocationValue> parse(CodeBlock block) {
        // Получаем координаты из параметров
        Object xObj = block.getParameter(xParameter);
        Object yObj = block.getParameter(yParameter);
        Object zObj = block.getParameter(zParameter);
        
        if (xObj == null || yObj == null || zObj == null) {
            return Optional.empty();
        }
        
        String xStr = xObj.toString();
        String yStr = yObj.toString();
        String zStr = zObj.toString();
        
        // Проверяем, что координаты не пустые
        if (xStr.trim().isEmpty() || yStr.trim().isEmpty() || zStr.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // Получаем мир, если указан
        String worldStr = null;
        if (worldParameter != null) {
            Object worldObj = block.getParameter(worldParameter);
            if (worldObj != null) {
                worldStr = worldObj.toString();
            }
        }
        
        // Создаем LocationValue
        LocationValue locationValue = new LocationValue(xStr, yStr, zStr, worldStr);
        return Optional.of(locationValue);
    }

    /**
     * Возвращает имена параметров координат.
     * @return Массив с именами параметров [x, y, z, world]
     */
    public String[] getParameterNames() {
        return new String[]{xParameter, yParameter, zParameter, worldParameter};
    }
} 