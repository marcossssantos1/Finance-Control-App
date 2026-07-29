package com.financeapp.repository;

import com.financeapp.domain.Transacao;
import com.financeapp.repository.projection.TotalPorCategoriaProjection;
import com.financeapp.repository.projection.TotalPorTipoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    @Query("""
        SELECT t FROM Transacao t
        WHERE t.usuario.id = :usuarioId
          AND (:dataInicio IS NULL OR t.dataTransacao >= :dataInicio)
          AND (:dataFim IS NULL OR t.dataTransacao <= :dataFim)
          AND (:contaId IS NULL OR t.conta.id = :contaId)
          AND (:categoriaId IS NULL OR t.categoria.id = :categoriaId)
        ORDER BY t.dataTransacao DESC, t.id DESC
        """)
    Page<Transacao> buscarComFiltros(
            @Param("usuarioId") Long usuarioId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("contaId") Long contaId,
            @Param("categoriaId") Long categoriaId,
            Pageable pageable
    );

    List<Transacao> findAllByContaId(Long contaId);

    Optional<Transacao> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Query("""
        SELECT t.tipo AS tipo, SUM(t.valor) AS total
        FROM Transacao t
        WHERE t.usuario.id = :usuarioId
          AND (:dataInicio IS NULL OR t.dataTransacao >= :dataInicio)
          AND (:dataFim IS NULL OR t.dataTransacao <= :dataFim)
        GROUP BY t.tipo
        """)
    List<TotalPorTipoProjection> somarPorTipo(
            @Param("usuarioId") Long usuarioId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    @Query("""
        SELECT c.id AS categoriaId, c.nome AS categoriaNome, t.tipo AS tipo, SUM(t.valor) AS total
        FROM Transacao t
        JOIN t.categoria c
        WHERE t.usuario.id = :usuarioId
          AND (:dataInicio IS NULL OR t.dataTransacao >= :dataInicio)
          AND (:dataFim IS NULL OR t.dataTransacao <= :dataFim)
        GROUP BY c.id, c.nome, t.tipo
        ORDER BY total DESC
        """)
    List<TotalPorCategoriaProjection> somarPorCategoria(
            @Param("usuarioId") Long usuarioId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );
}
