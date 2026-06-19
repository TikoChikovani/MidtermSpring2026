package uno.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PlayerMapper {

    @Select("SELECT id, name FROM players WHERE name = #{name}")
    PlayerRecord findByName(@Param("name") String name);

    @Insert("INSERT INTO players(name) VALUES(#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PlayerRecord player);
}
