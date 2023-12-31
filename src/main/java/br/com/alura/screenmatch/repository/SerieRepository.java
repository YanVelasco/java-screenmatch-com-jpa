package br.com.alura.screenmatch.repository;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SerieRepository extends JpaRepository<Serie, UUID> {
    Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);

    List<Serie> findByGenero(Categoria categoria);

    List<Serie> findByAtoresContainingIgnoreCase(String atores);

    List<Serie> findTop5ByOrderByAvaliacaoDesc();
    @Query("SELECT s FROM Serie s WHERE s.totalTemporadas <= :totalTemporadas AND s.avaliacao >= :avaliacao")
    List<Serie> findByTotalTemporadasLessThanOrEqualAndAvaliacaoGreaterThanOrEqual(@Param("totalTemporadas") Integer totalTemporadas, @Param("avaliacao") Double avaliacao);
}