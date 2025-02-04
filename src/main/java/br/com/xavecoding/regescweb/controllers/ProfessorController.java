package br.com.xavecoding.regescweb.controllers;

import br.com.xavecoding.regescweb.dto.RequisicaoFormProfessor;
import br.com.xavecoding.regescweb.models.Professor;
import br.com.xavecoding.regescweb.models.StatusProfessor;
import br.com.xavecoding.regescweb.repositories.ProfessorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping(value = "/professores")
public class ProfessorController {

    @Autowired
    private ProfessorRepository professorRepository;

    @GetMapping("")
    public ModelAndView index() {
        List<Professor> professores = this.professorRepository.findAll();
        ModelAndView mv = new ModelAndView("professores/index");
        mv.addObject("professores", professores);

        return mv;
    }

    @GetMapping("/new")
    public ModelAndView nnew(RequisicaoFormProfessor requisicao) {
        ModelAndView mv = new ModelAndView("professores/new");
        mv.addObject("listaStatusProfessor", StatusProfessor.values());
        return mv;
    }

    // web parameter tampering
    @PostMapping("")
    public ModelAndView create(@Valid RequisicaoFormProfessor requisicao, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            System.out.println("\n*************  TEM ERROS *************\n");

            ModelAndView mv = new ModelAndView("professores/new");
            mv.addObject("listaStatusProfessor", StatusProfessor.values());
            return mv;
        } else {
            Professor professor = requisicao.toProfessor();
            this.professorRepository.save(professor);


            return new ModelAndView("redirect:/professores/" + professor.getId());
        }
    }

    @GetMapping("/{id}")
    public ModelAndView show(@PathVariable Long id) {
        Optional<Professor> optional = this.professorRepository.findById(id); // Para poder retornar nulo do bd

        if (optional.isPresent()) {
            Professor professor = optional.get();
            ModelAndView mv = new ModelAndView("professores/show");
            mv.addObject("professor", professor);

            return mv;
        }
        // nao achou um registro na tabela Professor com o id informado
        else {
            System.out.println("######### NAO ACHOU O PROFESSOR DE ID " + id + " #########");

            return this.retornaErroProfessor("SHOW ERROR: Professor #" + id + " não encontrado no banco!");
        }

    }


    @GetMapping("/{id}/edit")
    public ModelAndView edit(@PathVariable Long id, RequisicaoFormProfessor requisicao) {


        Optional<Professor> optional = this.professorRepository.findById(id);

        if (optional.isPresent()) {
            Professor professor = optional.get();
            requisicao.fromProfessor(professor);

            ModelAndView mv = new ModelAndView("professores/edit");
            mv.addObject("professorId", professor.getId());
            mv.addObject("listaStatusProfessor", StatusProfessor.values());

            return mv;

        }
        // nao achou um registro na tabela Professor com o id informado
        else {
            System.out.println("######### NAO ACHOU O PROFESSOR DE ID " + id + " #########");
            return this.retornaErroProfessor("EDIT ERROR: Professor #" + id + " não encontrado no banco!");
        }

    }

    @PostMapping("/{id}")
    public ModelAndView update(@PathVariable Long id, @Valid RequisicaoFormProfessor requisicao, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            ModelAndView mv = new ModelAndView("professores/edit");
            mv.addObject("professorId", id);
            mv.addObject("listaStatusProfessor", StatusProfessor.values());

            return mv;
        } else {

            Optional<Professor> optional = this.professorRepository.findById(id); // Para poder retornar nulo do bd

            if (optional.isPresent()) {
                Professor professor = requisicao.toProfessor(optional.get());
                this.professorRepository.save(professor); // agora ele identifica o id e salva no registro certo

                return new ModelAndView("redirect:/professores/" + professor.getId());
                /*
                Não é convencional deixar lógica de negócio aqui, colocar no RequisicaoFormProfessor ou no Professor

                professor.setNome(requisicao.getNome());
                professor.setSalario(requisicao.getSalario());
                professor.setStatusProfessor(requisicao.getStatusProfessor());

                */
            }
            // nao achou um registro na tabela Professor com o id informado
            else {
                System.out.println("######### NAO ACHOU O PROFESSOR DE ID " + id + " #########");
                return this.retornaErroProfessor("UPDATE ERROR: Professor #" + id + " não encontrado no banco!");
            }



            /*
            Se eu fizer com as linhas abaixo, sera criado um registro novo pq n tem id

            Professor professor = requisicao.toProfessor();
            this.professorRepository.save(professor);
            return new ModelAndView("redirect:/professores/" + professor.getId());
            */
        }
    }

    @GetMapping("/{id}/delete")
    public ModelAndView /* String */ delete(@PathVariable Long id) {
        ModelAndView mv = new ModelAndView("redirect:/professores");
        try {
            this.professorRepository.deleteById(id);
            mv.addObject("mensagem", "Professor #" + id + " deletado com sucesso!");
            mv.addObject("erro", false);
//            return "redirect:/professores?mensagem="; Pode colocar a mensagem aqui
        } catch (EmptyResultDataAccessException e) {
            System.out.println(e);
            mv = retornaErroProfessor("DELETE ERROR: Professor #" + id + " não encontrado no banco!");
//            return "redirect:/professores";   Caso use string
        }
        return mv;
    }

    private ModelAndView retornaErroProfessor(String msg) {
        ModelAndView mv = new ModelAndView("redirect:/professores");
        mv.addObject("mensagem", msg);
        mv.addObject("erro", true);
        return mv;
    }
}
