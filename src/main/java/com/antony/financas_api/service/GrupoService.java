package com.antony.financas_api.service;

import com.antony.financas_api.dto.GrupoDetalheDTO;
import com.antony.financas_api.dto.GrupoRequestDTO;
import com.antony.financas_api.dto.GrupoResponseDTO;
import com.antony.financas_api.exception.AcessoNegadoException;
import com.antony.financas_api.exception.ConflitoException;
import com.antony.financas_api.model.Grupo;
import com.antony.financas_api.model.Usuario;
import com.antony.financas_api.repository.GrupoRepository;
import com.antony.financas_api.repository.TransacaoRepository;
import com.antony.financas_api.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Regras de grupo num lugar só. A principal: só quem é membro vê ou mexe no grupo
@Service
public class GrupoService {

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    // 404 se o grupo não existe, 403 se existe mas o usuário não é membro
    public Grupo buscarDoMembro(Long idGrupo, Long idUsuario) {
        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado"));
        boolean fazParte = grupo.getUsuarios().stream().anyMatch(u -> u.getId().equals(idUsuario));
        if (!fazParte) {
            throw new AcessoNegadoException("Acesso negado: você não faz parte deste grupo.");
        }
        return grupo;
    }

    public GrupoResponseDTO criar(GrupoRequestDTO dto, Long idUsuario) {
        Usuario criador = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        Grupo grupo = new Grupo();
        grupo.setNome(dto.nome().trim());
        grupo.setDescricao(dto.descricao() == null || dto.descricao().isBlank() ? null : dto.descricao().trim());
        // Quem cria já entra como membro (o JPA preenche a tabela grupo_usuarios)
        grupo.getUsuarios().add(criador);
        return new GrupoResponseDTO(grupoRepository.save(grupo));
    }

    public List<GrupoResponseDTO> listarDoUsuario(Long idUsuario) {
        return grupoRepository.findByUsuariosIdOrderByNomeAsc(idUsuario).stream()
                .map(GrupoResponseDTO::new).toList();
    }

    public GrupoDetalheDTO detalhar(Long idGrupo, Long idUsuario) {
        return new GrupoDetalheDTO(buscarDoMembro(idGrupo, idUsuario), idUsuario);
    }

    @Transactional
    public GrupoDetalheDTO adicionarMembroPorLogin(Long idGrupo, String login, Long idUsuario) {
        Grupo grupo = buscarDoMembro(idGrupo, idUsuario);
        Usuario novoMembro = usuarioRepository.findFirstByLoginIgnoreCase(login.trim())
                .orElseThrow(() -> new EntityNotFoundException("Nenhuma conta com o e-mail " + login.trim()));
        if (grupo.getUsuarios().contains(novoMembro)) {
            throw new ConflitoException(novoMembro.getLogin() + " já faz parte do grupo");
        }
        grupo.getUsuarios().add(novoMembro);
        return new GrupoDetalheDTO(grupoRepository.save(grupo), idUsuario);
    }

    // Mantido para quem já usava a rota antiga, que recebe o id do usuário
    @Transactional
    public GrupoResponseDTO adicionarMembroPorId(Long idGrupo, Long idNovoMembro, Long idUsuario) {
        Grupo grupo = buscarDoMembro(idGrupo, idUsuario);
        Usuario novoMembro = usuarioRepository.findById(idNovoMembro)
                .orElseThrow(() -> new EntityNotFoundException("Usuário a ser adicionado não encontrado"));
        grupo.getUsuarios().add(novoMembro);
        return new GrupoResponseDTO(grupoRepository.save(grupo));
    }

    // @Transactional: desligar os gastos, sair e (se for o caso) apagar o grupo acontecem juntos ou nada acontece
    @Transactional
    public void sair(Long idGrupo, Long idUsuario) {
        Grupo grupo = buscarDoMembro(idGrupo, idUsuario);
        // Os gastos de quem sai deixam de ser do grupo (continuam na conta da pessoa).
        // Assim o acerto de contas só envolve quem ainda é membro e os saldos continuam somando 0
        transacaoRepository.desligarDoGrupo(idGrupo, idUsuario);
        grupo.getUsuarios().removeIf(u -> u.getId().equals(idUsuario));
        if (grupo.getUsuarios().isEmpty()) {
            // Último a sair: o grupo é apagado. Antes, solta qualquer gasto que ainda aponte para ele
            // (senão a chave estrangeira impede apagar)
            transacaoRepository.desligarTodasDoGrupo(idGrupo);
            grupoRepository.delete(grupo);
        } else {
            grupoRepository.save(grupo);
        }
    }
}
