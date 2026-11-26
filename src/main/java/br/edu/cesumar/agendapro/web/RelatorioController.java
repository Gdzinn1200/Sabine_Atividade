package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.regras.RegrasRelatorio;
import br.edu.cesumar.agendapro.repositorio.ServicoRepository;
import br.edu.cesumar.agendapro.servico.ProfissionalService;
import br.edu.cesumar.agendapro.servico.RelatorioService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

/** Relatorios de atendimentos (RF16). O acesso e restrito ao administrador (RN13). */
@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioService relatorios;
    private final ProfissionalService profissionais;
    private final ServicoRepository servicos;

    public RelatorioController(RelatorioService relatorios,
                               ProfissionalService profissionais,
                               ServicoRepository servicos) {
        this.relatorios = relatorios;
        this.profissionais = profissionais;
        this.servicos = servicos;
    }

    @GetMapping
    public String gerar(@RequestParam(required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                        @RequestParam(required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                        @RequestParam(required = false) Long profissional,
                        @RequestParam(required = false) Long servico,
                        Model model) {

        LocalDate inicio = de == null ? LocalDate.now().minusDays(30) : de;
        LocalDate fim = ate == null ? LocalDate.now() : ate;

        model.addAttribute("de", inicio);
        model.addAttribute("ate", fim);
        model.addAttribute("profissional", profissional);
        model.addAttribute("servico", servico);
        model.addAttribute("profissionais", profissionais.listarAtivos());
        model.addAttribute("servicos", servicos.findByAtivoTrueOrderByNome());

        var erros = RegrasRelatorio.validarPeriodo(inicio, fim);
        if (!erros.isEmpty()) {
            model.addAttribute("erro", erros.get(0).mensagemCompleta());
            return "relatorios/index";
        }

        List<RegrasRelatorio.Atendimento> dados =
                relatorios.atendimentos(inicio, fim, profissional, servico);

        model.addAttribute("resumo", RegrasRelatorio.resumir(dados));
        model.addAttribute("porProfissional", RegrasRelatorio.porProfissional(dados));
        model.addAttribute("porServico", RegrasRelatorio.porServico(dados));
        model.addAttribute("vazio", dados.isEmpty());
        return "relatorios/index";
    }

    /** Mesmo relatorio da tela, em CSV, para a conferencia em planilha (RF16). */
    @GetMapping("/csv")
    public ResponseEntity<String> csv(@RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                      @RequestParam(required = false) Long profissional,
                                      @RequestParam(required = false) Long servico) {

        LocalDate inicio = de == null ? LocalDate.now().minusDays(30) : de;
        LocalDate fim = ate == null ? LocalDate.now() : ate;

        var erros = RegrasRelatorio.validarPeriodo(inicio, fim);
        if (!erros.isEmpty()) {
            return ResponseEntity.badRequest().body(erros.get(0).mensagemCompleta());
        }

        List<RegrasRelatorio.Atendimento> dados =
                relatorios.atendimentos(inicio, fim, profissional, servico);

        StringBuilder csv = new StringBuilder("data;profissional;servico;situacao;valor\n");
        var formato = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (RegrasRelatorio.Atendimento a : dados) {
            csv.append(a.data().format(formato)).append(';')
               .append(a.profissional()).append(';')
               .append(a.servico()).append(';')
               .append(a.situacao().getRotulo()).append(';')
               .append(a.valor().toPlainString().replace('.', ',')).append('\n');
        }

        String nome = "relatorio_agendapro_" + inicio + "_a_" + fim + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome + "\"")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(csv.toString());
    }
}
