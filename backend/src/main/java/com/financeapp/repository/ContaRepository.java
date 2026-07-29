package com.financeapp.repository;

import com.financeapp.domain.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    List<Conta> findAllByUsuarioIdOrderByNomeAsc(Long usuarioId);

    Optional<Conta> findByIdAndUsuarioId(Long id, Long usuarioId);
}
