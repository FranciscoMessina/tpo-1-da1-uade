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

    @Query("SELECT * FROM publicaciones WHERE status IS NULL OR status = 'active' ORDER BY cachedAt DESC")
    List<PublicacionEntity> getPublicacionesHome();

    @Query("SELECT * FROM publicaciones WHERE id = :id LIMIT 1")
    PublicacionEntity getById(String id);

    @Query("SELECT * FROM publicaciones WHERE lastConsultedAt > 0 ORDER BY lastConsultedAt DESC LIMIT :limit")
    List<PublicacionEntity> getUltimasConsultadas(int limit);

    @Query("UPDATE publicaciones SET lastConsultedAt = :timestamp WHERE id = :id")
    void actualizarUltimaConsulta(String id, long timestamp);

    @Query("UPDATE publicaciones SET localCoverImagePath = :localPath WHERE id = :id")
    void actualizarLocalCover(String id, String localPath);

    @Query("UPDATE publicaciones SET fullJson = :fullJson WHERE id = :id")
    void actualizarFullJson(String id, String fullJson);
}
