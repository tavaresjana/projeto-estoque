package com.projeto.estoque_poc.controller;

import com.projeto.estoque_poc.model.Usuario;
import com.projeto.estoque_poc.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/cadastro")
    public String mostrarFormularioCadastro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "cadastro-usuario";
    }

    @PostMapping("/cadastro")
    public String cadastrarUsuario(@ModelAttribute("usuario") Usuario usuario) {
        usuarioService.salvarUsuario(usuario);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String mostrarFormularioLogin(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "login";
    }

    @PostMapping("/login")
    public String loginUsuario(@ModelAttribute("usuario") Usuario usuario, Model model) {
        boolean autenticado = usuarioService.autenticarUsuario(usuario.getEmail(), usuario.getSenha());

        if (autenticado) {
            Optional<Usuario> usuarioOpt = usuarioService.buscarUsuarioPorEmail(usuario.getEmail());
            if (usuarioOpt.isPresent()) {
                Usuario usuarioAutenticado = usuarioOpt.get();

                // Lógica de redirecionamento com base na role
                if ("ADMIN".equalsIgnoreCase(usuarioAutenticado.getRole())) {
                    return "redirect:/";  // Página inicial do ADMIN (ou outra página principal)
                } else if ("FINAC".equalsIgnoreCase(usuarioAutenticado.getRole())) {
                    return "redirect:/finac/relatorio";  // Página para usuários FINAC
                } else if ("USER".equalsIgnoreCase(usuarioAutenticado.getRole())) {
                    return "redirect:/user/produtos";  // Página de produtos para o usuário tipo USER
                }
            }
        }

        // Caso a autenticação falhe ou os dados estejam incorretos
        model.addAttribute("errorMessage", "Email ou senha incorretos");
        return "login";  // Volta para a tela de login com a mensagem de erro
    }

    @GetMapping("/user/produtos")
    public String produtosUsuario() {
        // Lógica para exibir os produtos do usuário
        return "user/produtos";  // Certifique-se de que o template user/produtos.html existe
    }

    @GetMapping("/finac/relatorio")
    public String relatorioFinac() {
        // Lógica para exibir relatório financeiro para usuários FINAC
        return "finac/relatorio";  // Certifique-se de que o template finac/relatorio.html existe
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Remover todos os atributos da sessão
        session.invalidate();
        // Redireciona o usuário para a página de login após o logout
        return "redirect:/login";
    }
}
