package com.antony.financas_api.controller;

import com.antony.financas_api.model.Categoria;
import com.antony.financas_api.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 1. Avisa o Spring que essa classe vai responder requisições da internet
@RestController
// 2. Define o endereço base. Tudo que chegar em "localhost:8080/categorias" cai aqui
@RequestMapping("/categorias")
public class CategoriaController {

    // 3. O @Autowired injeta a interface que criamos antes.
    // É como se o Spring dissesse: "Deixa que eu instancio o banco para você usar"
    @Autowired
    private CategoriaRepository repository;

    // 4. O @PostMapping escuta requisições do tipo POST.
    // O @RequestBody avisa que os dados da categoria virão no "corpo" da requisição.
    @PostMapping
    public Categoria criarCategoria(@RequestBody Categoria categoria) {
        // Pega a categoria recebida e manda o repository salvar no banco
        return repository.save(categoria);
    }

    // 5. O @GetMapping escuta requisições do tipo GET (quando pedem informações).
    @GetMapping
    public List<Categoria> listarCategorias() {
        // Vai no banco, pega todas as categorias e devolve em formato de lista
        return repository.findAll();
    }
}