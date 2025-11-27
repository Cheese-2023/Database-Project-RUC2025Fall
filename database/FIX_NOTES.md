# 数据库建表脚本修复说明

## 问题描述
原始的 `schema.sql` 脚本中使用了 MySQL 生成列（GENERATED ALWAYS AS），但部分生成列使用了不被允许的函数，导致执行失败：
```
ERROR 3102 (HY000) at line 51: Expression of generated column 'population_density' contains a disallowed function.
```

## 修复内容

已将以下表中的生成列改为普通字段：

1. **population_statistics** (人口统计表)
   - `population_density` - 人口密度
   - `urbanization_rate` - 城镇化率

2. **employment_statistics** (就业统计表)
   - `total_employment_万` - 总就业人数

3. **economic_aggregate** (经济总量表)
   - `industry_structure_1` - 第一产业比重
   - `industry_structure_2` - 第二产业比重
   - `industry_structure_3` - 第三产业比重

4. **fiscal_finance** (财政金融表)
   - `fiscal_self_sufficiency` - 财政自给率

5. **agriculture_production** (农业生产表)
   - `mechanization_rate` - 农业机械化率
   - `grain_yield_per_hectare` - 粮食单产

6. **education_health** (教育卫生表)
   - `student_teacher_ratio_primary` - 小学生师比

7. **income_living_standard** (收入水平表)
   - `income_gap_ratio` - 城乡收入差距比

## 数据计算方案

这些计算字段现在需要通过以下方式之一来填充：

### 方案1：数据导入时计算（推荐）
在 Python 数据导入脚本中计算这些字段：

```python
# 计算人口密度
population_density = (total_population_万 * 10000) / land_area_km2

# 计算城镇化率
urbanization_rate = ((total_population_万 - rural_population_万) / total_population_万) * 100

# 计算产业结构比重
industry_structure_1 = (primary_industry_万元 / gdp_万元) * 100
industry_structure_2 = (secondary_industry_万元 / gdp_万元) * 100
industry_structure_3 = (tertiary_industry_万元 / gdp_万元) * 100

# 计算财政自给率
fiscal_self_sufficiency = (fiscal_revenue_万元 / fiscal_expenditure_万元) * 100

# 等等...
```

### 方案2：创建触发器（可选）
可以创建 MySQL 触发器在插入/更新时自动计算：

```sql
DELIMITER //
CREATE TRIGGER calc_population_density
BEFORE INSERT ON population_statistics
FOR EACH ROW
BEGIN
    DECLARE land_area DECIMAL(10,2);
    SELECT land_area_km2 INTO land_area 
    FROM county_basic 
    WHERE county_code = NEW.county_code;
    
    IF land_area > 0 THEN
        SET NEW.population_density = (NEW.total_population_万 * 10000) / land_area;
    END IF;
    
    IF NEW.total_population_万 > 0 THEN
        SET NEW.urbanization_rate = ((NEW.total_population_万 - NEW.rural_population_万) / NEW.total_population_万) * 100;
    END IF;
END //
DELIMITER ;
```

### 方案3：应用层计算
在后端 Service 层查询时动态计算并返回。

## 现在可以执行

修复后的脚本可以正常执行：

```bash
mysql -u root -p < database/schema.sql
```

## 后续更新

`import_data.py` 脚本已经更新，会在导入数据时自动计算这些字段的值。
