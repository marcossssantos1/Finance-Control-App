package com.financeapp.repository;

import com.financeapp.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    @Query("SELECT c FROM Categoria c WHERE c.usuario IS NULL OR c.usuario.id = :usuarioId ORDER BY c.tipo, c.nome")
    List<Categoria> findDisponiveisParaUsuario(@Param("usuarioId") Long usuarioId);

    // Não encontra categorias padrão (usuario_id null) de propósito:
    // só o dono pode "gerenciar" a categoria customizada, ainda que qualquer
    // usuário possa USAR uma categoria padrão para classificar transações.
    Optional<Categoria> findByIdAndUsuarioId(Long id, Long usuarioId);
}
