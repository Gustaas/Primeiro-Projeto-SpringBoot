package br.com.projeto.api.repositorio;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.projeto.api.modelo.Consulta;
    
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByUserId(Long userId);
}
