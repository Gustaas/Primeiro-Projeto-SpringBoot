package br.com.projeto.api.controle;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import br.com.projeto.api.eventoapp.UsuarioRepository;
import br.com.projeto.api.modelo.Consulta;
import br.com.projeto.api.modelo.PacienteDTO;
import br.com.projeto.api.modelo.Usuario;
import br.com.projeto.api.repositorio.ConsultaRepository;
import br.com.projeto.api.service.UsuarioService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class EventoController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @RequestMapping(value="/cadconsulta", method=RequestMethod.GET)
    public String form() {
        return "index";
    }

    @RequestMapping(value="/criarconsulta", method=RequestMethod.GET)
    public String consulta() {
        return "consulta";
    }

    @RequestMapping(value="/login", method=RequestMethod.GET)
    public String login() {
        return "login";
    }

    @RequestMapping(value="/consultasCliente", method=RequestMethod.GET)
    public String consultas() {
        return "consultas";
    }

    @GetMapping("/consultas")
    public String mostrarConsultas(HttpServletRequest request, Model model) {
        Long userId = getUserIdFromCookies(request);
        if (userId == null) {
            return "redirect:/login";
        }
        List<Consulta> consultas = consultaRepository.findByUserId(userId);
        model.addAttribute("consultas", consultas);

        return "consultas";
    }

    @GetMapping("/api/consultas")
    @ResponseBody
    public List<Consulta> getConsultas(HttpServletRequest request) {
        Long userId = getUserIdFromCookies(request);
        if (userId == null) {
            return null; // Ou lançar uma exceção adequada
        }
        return consultaRepository.findByUserId(userId);
    }

    @GetMapping("/api/paciente/nome")
    @ResponseBody
    public PacienteDTO getPaciente(HttpServletRequest request) {
        Long userId = getUserIdFromCookies(request);
        if (userId == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }
        Usuario usuario = usuarioRepository.findById(userId).orElse(null);
        if (usuario != null) {
            String nome = usuario.getNome();
            String cpf = usuario.getCpf();
            return new PacienteDTO(nome, cpf);
        }
        return new PacienteDTO("", "");
    }

    @DeleteMapping("/api/consultas/{id}")
    @ResponseBody
    public void deleteConsulta(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        Long userId = getUserIdFromCookies(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        consultaRepository.deleteById(id);
    }

    private Long getUserIdFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("userId".equals(cookie.getName()) && cookie.getValue() != null) {
                    return Long.parseLong(cookie.getValue());
                }
            }
        }
        return null;
    }



    @PostMapping("/cadastro")
    public String cadastrarUsuario(@Validated @ModelAttribute Usuario usuario) {
        usuarioService.salvarUsuario(usuario);
        return "redirect:/login";
    }

    @PostMapping("/cadastroConsulta")
    public String salvarCadastro(@RequestParam String especialidade, @RequestParam String dia, @RequestParam String mes, @RequestParam String doutor, @RequestParam String horario, HttpServletRequest request) {
        Long userId = null;
        int userIdAsInt = 0;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("userId".equals(cookie.getName())) {
                    userId = Long.parseLong(cookie.getValue());
                    
                    userIdAsInt = userId.intValue();
                    break;
                }
            }
        }
        
        if (userId == null) {
            return "redirect:/login";
        }

        Consulta consulta = new Consulta();
        consulta.setUserId(userIdAsInt);
        consulta.setEspecialidade(especialidade);
        consulta.setDia(dia);
        consulta.setMes(mes);
        consulta.setDoutor(doutor);
        consulta.setHorario(horario);
        consultaRepository.save(consulta);

        return "redirect:/cadconsulta";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String senha, HttpServletResponse response) {
        Usuario user = usuarioService.autenticarUsuario(email, senha);
        if (user != null) {
            Cookie cookie = new Cookie("userId", Long.toString(user.getId()));
            cookie.setMaxAge(3600);
            cookie.setPath("/");
            response.addCookie(cookie);
            
            return "redirect:/cadconsulta";
        } else {
            return "redirect:login";
        }
    }

}