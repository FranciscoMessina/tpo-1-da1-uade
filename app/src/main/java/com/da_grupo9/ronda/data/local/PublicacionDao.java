package com.da_grupo9.ronda.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PublicacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(PublicacionEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateAll(List<PublicacionEntity> entities);

    @Query("SELECT * FROM publicaciones WHERE accountId = :accountId AND (status IS NULL OR status = 'active') ORDER BY cachedAt DESC")
    List<PublicacionEntity> getPublicacionesHome(String accountId);

    @Query("SELECT * FROM publicaciones WHERE accountId = :accountId AND id = :id LIMIT 1")
    PublicacionEntity getById(String accountId, String id);

    @Query("SELECT * FROM publicaciones WHERE accountId = :accountId AND lastConsultedAt > 0 ORDER BY lastConsultedAt DESC LIMIT :limit")
    List<PublicacionEntity> getUltimasConsultadas(String accountId, int limit);

    @Query("UPDATE publicaciones SET lastConsultedAt = :timestamp WHERE accountId = :accountId AND id = :id")
    void actualizarUltimaConsulta(String accountId, String id, long timestamp);
}
