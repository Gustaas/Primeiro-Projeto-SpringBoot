package br.com.projeto.api.eventoapp;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.projeto.api.modelo.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmailAndSenha(String email, String senha);
}
