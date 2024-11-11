package com.projeto.estoque_poc.service;

import ch.qos.logback.core.model.Model;
import com.projeto.estoque_poc.model.Usuario;
import com.projeto.estoque_poc.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RelatoriosService relatoriosService;

    @Autowired
    private ProdutoService produtoService;

    public void salvarUsuario(Usuario usuario) {
        usuarioRepository.save(usuario);
    }

    public boolean autenticarUsuario(String email, String senha) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        return usuarioOpt.isPresent() && usuarioOpt.get().getSenha().equals(senha);
    }

    // Método para buscar o usuário pelo email
    public Optional<Usuario> buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

}
