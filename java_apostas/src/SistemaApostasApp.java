import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SistemaApostasApp extends JFrame {

    private final Sistema sistema = new Sistema();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JComboBox<Campeonato> comboCampeonatoClubes = new JComboBox<>();
    private final JComboBox<Campeonato> comboCampeonatoPartida = new JComboBox<>();
    private final JComboBox<Clube> comboClubeCampeonato = new JComboBox<>();
    private final JComboBox<Clube> comboMandantePartida = new JComboBox<>();
    private final JComboBox<Clube> comboVisitantePartida = new JComboBox<>();
    private final JComboBox<Participante> comboParticipanteGrupo = new JComboBox<>();
    private final JComboBox<GrupoApostas> comboGrupo = new JComboBox<>();
    private final JComboBox<Participante> comboParticipanteAposta = new JComboBox<>();
    private final JComboBox<Partida> comboPartidaAposta = new JComboBox<>();
    private final JComboBox<Partida> comboPartidaResultado = new JComboBox<>();
    private final JComboBox<GrupoApostas> comboGrupoRanking = new JComboBox<>();

    private final DefaultListModel<Clube> modeloClubes = new DefaultListModel<>();
    private final DefaultListModel<Campeonato> modeloCampeonatos = new DefaultListModel<>();
    private final DefaultListModel<Partida> modeloPartidas = new DefaultListModel<>();
    private final DefaultListModel<Participante> modeloParticipantes = new DefaultListModel<>();
    private final DefaultListModel<GrupoApostas> modeloGrupos = new DefaultListModel<>();
    private final JTextArea areaLog = new JTextArea();
    private final JTextArea areaRanking = new JTextArea();

    public SistemaApostasApp() {
        super("Sistema de Apostas - Campeonato de Futebol");
        configurarJanela();
        sistema.carregarDadosExemplo();
        criarInterface();
        atualizarTudo();
        registrarLog("Sistema iniciado com dados de exemplo.");
    }

    private void configurarJanela() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void criarInterface() {
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Campeonatos e Clubes", criarPainelCampeonatos());
        abas.addTab("Partidas", criarPainelPartidas());
        abas.addTab("Grupos e Participantes", criarPainelGrupos());
        abas.addTab("Apostas e Resultados", criarPainelApostas());
        abas.addTab("Classificação", criarPainelClassificacao());

        add(abas, BorderLayout.CENTER);
        add(criarPainelLog(), BorderLayout.SOUTH);
    }

    private JPanel criarPainelCampeonatos() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel formularios = new JPanel(new GridLayout(1, 2, 10, 10));
        formularios.add(criarFormularioClube());
        formularios.add(criarFormularioCampeonato());

        JPanel listas = new JPanel(new GridLayout(1, 2, 10, 10));
        listas.add(criarListaComTitulo("Clubes cadastrados", new JList<>(modeloClubes)));
        listas.add(criarListaComTitulo("Campeonatos cadastrados", new JList<>(modeloCampeonatos)));

        painel.add(formularios, BorderLayout.NORTH);
        painel.add(listas, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarFormularioClube() {
        JPanel painel = criarPainelFormulario("Cadastro de clube");

        JTextField campoNome = new JTextField();
        JTextField campoCidade = new JTextField();
        JButton botao = new JButton("Cadastrar clube");

        botao.addActionListener(e -> {
            try {
                sistema.cadastrarClube(campoNome.getText(), campoCidade.getText());
                registrarLog("Clube cadastrado: " + campoNome.getText().trim());
                campoNome.setText("");
                campoCidade.setText("");
                atualizarTudo();
            } catch (RegraNegocioException ex) {
                mostrarErro(ex.getMessage());
            }
        });

        painel.add(new JLabel("Nome:"));
        painel.add(campoNome);
        painel.add(new JLabel("Cidade:"));
        painel.add(campoCidade);
        painel.add(new JLabel());
        painel.add(botao);
        return painel;
    }

    private JPanel criarFormularioCampeonato() {
        JPanel painel = criarPainelFormulario("Cadastro de campeonato");

        JTextField campoNome = new JTextField();
        JButton cadastrar = new JButton("Cadastrar campeonato");
        JButton vincular = new JButton("Adicionar clube ao campeonato");

        cadastrar.addActionListener(e -> {
            try {
                sistema.cadastrarCampeonato(campoNome.getText());
                registrarLog("Campeonato cadastrado: " + campoNome.getText().trim());
                campoNome.setText("");
                atualizarTudo();
            } catch (RegraNegocioException ex) {
                mostrarErro(ex.getMessage());
            }
        });

        vincular.addActionListener(e -> {
            try {
                Campeonato campeonato = (Campeonato) comboCampeonatoClubes.getSelectedItem();
                Clube clube = (Clube) comboClubeCampeonato.getSelectedItem();
                sistema.vincularClubeAoCampeonato(campeonato, clube);
                registrarLog("Clube " + clube.getNome() + " adicionado ao campeonato " + campeonato.getNome());
                atualizarTudo();
            } catch (RegraNegocioException ex) {
                mostrarErro(ex.getMessage());
            }
        });

        painel.add(new JLabel("Nome do campeonato:"));
        painel.add(campoNome);
        painel.add(new JLabel());
        painel.add(cadastrar);
        painel.add(new JLabel("Campeonato:"));
        painel.add(comboCampeonatoClubes);
        painel.add(new JLabel("Clube:"));
        painel.add(comboClubeCampeonato);
        painel.add(new JLabel());
        painel.add(vincular);
        return painel;
    }

    private JPanel criarPainelPartidas() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel formulario = criarPainelFormulario("Cadastro de partida");
        JTextField campoDataHora = new JTextField("10/04/2026 20:00");
        JButton botaoCadastrar = new JButton("Cadastrar partida");

        comboCampeonatoPartida.addActionListener(e -> atualizarClubesDoCampeonatoSelecionado());

        botaoCadastrar.addActionListener(e -> {
            try {
                Campeonato campeonato = (Campeonato) comboCampeonatoPartida.getSelectedItem();
                Clube mandante = (Clube) comboMandantePartida.getSelectedItem();
                Clube visitante = (Clube) comboVisitantePartida.getSelectedItem();
                LocalDateTime dataHora = LocalDateTime.parse(campoDataHora.getText().trim(), formatter);
                sistema.cadastrarPartida(campeonato, mandante, visitante, dataHora);
                registrarLog("Partida cadastrada: " + mandante.getNome() + " x " + visitante.getNome());
                atualizarTudo();
            } catch (DateTimeParseException ex) {
                mostrarErro("Data inválida. Use o formato dd/MM/yyyy HH:mm");
            } catch (RegraNegocioException ex) {
                mostrarErro(ex.getMessage());
            }
        });

        formulario.add(new JLabel("Campeonato:"));
        formulario.add(comboCampeonatoPartida);
        formulario.add(new JLabel("Mandante:"));
        formulario.add(comboMandantePartida);
        formulario.add(new JLabel("Visitante:"));
        formulario.add(comboVisitantePartida);
        formulario.add(new JLabel("Data e hora:"));
        formulario.add(campoDataHora);
        formulario.add(new JLabel());
        formulario.add(botaoCadastrar);

        painel.add(formulario, BorderLayout.NORTH);
        painel.add(criarListaComTitulo("Partidas", new JList<>(modeloPartidas)), BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarPainelGrupos() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel formularios = new JPanel(new GridLayout(1, 2, 10, 10));
        formularios.add(criarFormularioParticipante());
        formularios.add(criarFormularioGrupo());

        JPanel listas = new JPanel(new GridLayout(1, 2, 10, 10));
        listas.add(criarListaComTitulo("Participantes", new JList<>(modeloParticipantes)));
        listas.add(criarListaComTitulo("Grupos", new JList<>(modeloGrupos)));

        painel.add(formularios, BorderLayout.NORTH);
        painel.add(listas, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarFormularioParticipante() {
        JPanel painel = criarPainelFormulario("Cadastro de participante");

        JTextField campoNome = new JTextField();
        JTextField campoEmail = new JTextField();
        JButton botao = new JButton("Cadastrar participante");

        botao.addActionListener(e -> {
            try {
                sistema.cadastrarParticipante(campoNome.getText(), campoEmail.getText());
                registrarLog("Participante cadastrado: " + campoNome.getText().trim());
                campoNome.setText("");
                campoEmail.setText("");
                atualizarTudo();
            } catch (RegraNegocioException ex) {
                mostrarErro(ex.getMessage());
            }
        });

        painel.add(new JLabel("Nome:"));
        painel.add(campoNome);
        painel.add(new JLabel("E-mail:"));
        painel.add(campoEmail);
        painel.add(new JLabel());
        painel.add(botao);
        return painel;
    }

    private JPanel criarFormularioGrupo() {
        JPanel painel = criarPainelFormulario("Cadastro de grupo");

        JTextField campoNome = new JTextField();
        JButton cadastrar = new JButton("Cadastrar grupo");
        JButton adicionar = new JButton("Adicionar participante ao grupo");

        cadastrar.addActionListener(e -> {
            try {
                sistema.cadastrarGrupo(campoNome.getText());
                registrarLog("Grupo cadastrado: " + campoNome.getText().trim());
                campoNome.setText("");
                atualizarTudo();
            } catch (RegraNegocioException ex) {
                mostrarErro(ex.getMessage());
            }
        });

        adicionar.addActionListener(e -> {
            try {
                GrupoApostas grupo = (GrupoApostas) comboGrupo.getSelectedItem();
                Participante participante = (Participante) comboParticipanteGrupo.getSelectedItem();
                sistema.adicionarParticipanteAoGrupo(grupo, participante);
                registrarLog("Participante " + participante.getNome() + " adicionado ao grupo " + grupo.getNome());
                atualizarTudo();
            } catch (RegraNegocioException ex) {
                mostrarErro(ex.getMessage());
            }
        });

        painel.add(new JLabel("Nome do grupo:"));
        painel.add(campoNome);
        painel.add(new JLabel());
        painel.add(cadastrar);
        painel.add(new JLabel("Grupo:"));
        painel.add(comboGrupo);
        painel.add(new JLabel("Participante:"));
        painel.add(comboParticipanteGrupo);
        painel.add(new JLabel());
        painel.add(adicionar);
        return painel;
    }

    private JPanel criarPainelApostas() {
        JPanel painel = new JPanel(new GridLayout(1, 2, 10, 10));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        painel.add(criarFormularioAposta());
        painel.add(criarFormularioResultado());
        return painel;
    }

    private JPanel criarFormularioAposta() {
        JPanel painel = criarPainelFormulario("Registro de aposta");

        JSpinner golsMandante = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        JSpinner golsVisitante = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        JButton botao = new JButton("Registrar aposta");

        botao.addActionListener(e -> {
            try {
                Participante participante = (Participante) comboParticipanteAposta.getSelectedItem();
                Partida partida = (Partida) comboPartidaAposta.getSelectedItem();
                sistema.registrarAposta(participante, partida, (int) golsMandante.getValue(), (int) golsVisitante.getValue());
                registrarLog("Aposta registrada para " + participante.getNome() + " na partida " + partida.getDescricaoCurta());
                atualizarTudo();
            } catch (RegraNegocioException ex) {
                mostrarErro(ex.getMessage());
            }
        });

        painel.add(new JLabel("Participante:"));
        painel.add(comboParticipanteAposta);
        painel.add(new JLabel("Partida:"));
        painel.add(comboPartidaAposta);
        painel.add(new JLabel("Gols mandante:"));
        painel.add(golsMandante);
        painel.add(new JLabel("Gols visitante:"));
        painel.add(golsVisitante);
        painel.add(new JLabel());
        painel.add(botao);
        return painel;
    }

    private JPanel criarFormularioResultado() {
        JPanel painel = criarPainelFormulario("Atualização do resultado");

        JSpinner golsMandante = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        JSpinner golsVisitante = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        JButton botao = new JButton("Registrar resultado");

        botao.addActionListener(e -> {
            try {
                Partida partida = (Partida) comboPartidaResultado.getSelectedItem();
                sistema.registrarResultado(partida, (int) golsMandante.getValue(), (int) golsVisitante.getValue());
                registrarLog("Resultado registrado: " + partida.getDescricaoCurta() + " = " + golsMandante.getValue() + " x " + golsVisitante.getValue());
                atualizarTudo();
            } catch (RegraNegocioException ex) {
                mostrarErro(ex.getMessage());
            }
        });

        painel.add(new JLabel("Partida:"));
        painel.add(comboPartidaResultado);
        painel.add(new JLabel("Gols mandante:"));
        painel.add(golsMandante);
        painel.add(new JLabel("Gols visitante:"));
        painel.add(golsVisitante);
        painel.add(new JLabel());
        painel.add(new JLabel());
        painel.add(new JLabel());
        painel.add(botao);
        return painel;
    }

    private JPanel criarPainelClassificacao() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topo = new JPanel(new BorderLayout(10, 10));
        JButton atualizar = new JButton("Atualizar classificação");
        atualizar.addActionListener(e -> atualizarRanking());

        topo.add(new JLabel("Grupo:"), BorderLayout.WEST);
        topo.add(comboGrupoRanking, BorderLayout.CENTER);
        topo.add(atualizar, BorderLayout.EAST);

        areaRanking.setEditable(false);
        areaRanking.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        painel.add(topo, BorderLayout.NORTH);
        painel.add(new JScrollPane(areaRanking), BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarPainelLog() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(new EmptyBorder(0, 10, 10, 10));
        areaLog.setRows(8);
        areaLog.setEditable(false);
        painel.add(criarTitulo("Log do sistema"), BorderLayout.NORTH);
        painel.add(new JScrollPane(areaLog), BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarPainelFormulario(String titulo) {
        JPanel painel = new JPanel(new GridLayout(0, 2, 8, 8));
        painel.setBorder(BorderFactory.createTitledBorder(titulo));
        return painel;
    }

    private JPanel criarListaComTitulo(String titulo, JList<?> lista) {
        JPanel painel = new JPanel(new BorderLayout());
        painel.add(criarTitulo(titulo), BorderLayout.NORTH);
        painel.add(new JScrollPane(lista), BorderLayout.CENTER);
        return painel;
    }

    private JLabel criarTitulo(String titulo) {
        JLabel label = new JLabel(titulo);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));
        return label;
    }

    private void atualizarTudo() {
        atualizarModelos();
        atualizarCombos();
        atualizarClubesDoCampeonatoSelecionado();
        atualizarRanking();
    }

    private void atualizarModelos() {
        preencherModelo(modeloClubes, sistema.getClubes());
        preencherModelo(modeloCampeonatos, sistema.getCampeonatos());
        preencherModelo(modeloPartidas, sistema.getPartidas());
        preencherModelo(modeloParticipantes, sistema.getParticipantes());
        preencherModelo(modeloGrupos, sistema.getGrupos());
    }

    private <T> void preencherModelo(DefaultListModel<T> modelo, List<T> itens) {
        modelo.clear();
        for (T item : itens) {
            modelo.addElement(item);
        }
    }

    private void atualizarCombos() {
        preencherCombo(comboCampeonatoClubes, sistema.getCampeonatos());
        preencherCombo(comboCampeonatoPartida, sistema.getCampeonatos());
        preencherCombo(comboClubeCampeonato, sistema.getClubes());
        preencherCombo(comboParticipanteGrupo, sistema.getParticipantes());
        preencherCombo(comboGrupo, sistema.getGrupos());
        preencherCombo(comboParticipanteAposta, sistema.getParticipantes());
        preencherCombo(comboPartidaAposta, sistema.getPartidas());
        preencherCombo(comboPartidaResultado, sistema.getPartidas());
        preencherCombo(comboGrupoRanking, sistema.getGrupos());
    }

    private <T> void preencherCombo(JComboBox<T> combo, List<T> itens) {
        Object selecionado = combo.getSelectedItem();
        combo.removeAllItems();
        for (T item : itens) {
            combo.addItem(item);
        }
        if (selecionado != null) {
            combo.setSelectedItem(selecionado);
        }
    }

    private void atualizarClubesDoCampeonatoSelecionado() {
        Campeonato campeonato = (Campeonato) comboCampeonatoPartida.getSelectedItem();
        List<Clube> clubes = campeonato == null ? sistema.getClubes() : campeonato.getClubes();
        preencherCombo(comboMandantePartida, clubes);
        preencherCombo(comboVisitantePartida, clubes);
    }

    private void atualizarRanking() {
        GrupoApostas grupo = (GrupoApostas) comboGrupoRanking.getSelectedItem();
        if (grupo == null) {
            areaRanking.setText("Cadastre um grupo para visualizar a classificação.");
            return;
        }

        List<Participante> ranking = sistema.classificacaoDoGrupo(grupo);
        StringBuilder sb = new StringBuilder();
        sb.append("Classificação do grupo: ").append(grupo.getNome()).append("\n\n");

        if (ranking.isEmpty()) {
            sb.append("Nenhum participante no grupo.");
        } else {
            int posicao = 1;
            for (Participante participante : ranking) {
                sb.append(posicao++)
                  .append(". ")
                  .append(participante.getNome())
                  .append(" - ")
                  .append(participante.calcularPontos())
                  .append(" pontos")
                  .append("\n");
            }
        }

        areaRanking.setText(sb.toString());
    }

    private void registrarLog(String mensagem) {
        areaLog.append("[OK] " + mensagem + "\n");
    }

    private void mostrarErro(String mensagem) {
        areaLog.append("[ERRO] " + mensagem + "\n");
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SistemaApostasApp().setVisible(true));
    }
}

interface Exibivel {
    String exibirResumo();
}

interface Pontuavel {
    int calcularPontos();
}

abstract class Pessoa implements Exibivel {
    private String nome;
    private String email;

    public Pessoa() {
        this("Sem nome", "sem-email@exemplo.com");
    }

    public Pessoa(String nome, String email) {
        setNome(nome);
        setEmail(email);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome não pode ficar em branco.");
        }
        this.nome = nome.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RegraNegocioException("O e-mail não pode ficar em branco.");
        }
        this.email = email.trim();
    }

    public abstract String getTipo();

    @Override
    public String exibirResumo() {
        return getTipo() + ": " + nome + " (" + email + ")";
    }
}

class Participante extends Pessoa implements Pontuavel {
    private final List<Aposta> apostas;
    private GrupoApostas grupo;

    public Participante() {
        this("Participante", "participante@exemplo.com");
    }

    public Participante(String nome, String email) {
        super(nome, email);
        this.apostas = new ArrayList<>();
    }

    public GrupoApostas getGrupo() {
        return grupo;
    }

    public void setGrupo(GrupoApostas grupo) {
        this.grupo = grupo;
    }

    public List<Aposta> getApostas() {
        return apostas;
    }

    public void adicionarAposta(Aposta aposta) {
        if (aposta == null) {
            throw new RegraNegocioException("A aposta não pode ser nula.");
        }
        apostas.add(aposta);
    }

    public void adicionarAposta(Partida partida, int golsMandante, int golsVisitante) {
        adicionarAposta(new Aposta(this, partida, golsMandante, golsVisitante));
    }

    @Override
    public int calcularPontos() {
        int total = 0;
        for (Aposta aposta : apostas) {
            total += aposta.calcularPontos();
        }
        return total;
    }

    @Override
    public String getTipo() {
        return "Participante";
    }

    @Override
    public String exibirResumo() {
        String grupoTexto = grupo == null ? "Sem grupo" : grupo.getNome();
        return super.exibirResumo() + " | Grupo: " + grupoTexto + " | Pontos: " + calcularPontos();
    }

    @Override
    public String toString() {
        return getNome();
    }
}

class Administrador extends Pessoa {
    public Administrador() {
        this("Administrador", "admin@exemplo.com");
    }

    public Administrador(String nome, String email) {
        super(nome, email);
    }

    @Override
    public String getTipo() {
        return "Administrador";
    }

    @Override
    public String exibirResumo() {
        return super.exibirResumo() + " | Responsável por registrar resultados";
    }
}

class Clube implements Exibivel {
    private String nome;
    private String cidade;

    public Clube() {
        this("Clube", "Cidade");
    }

    public Clube(String nome, String cidade) {
        setNome(nome);
        setCidade(cidade);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome do clube não pode ficar em branco.");
        }
        this.nome = nome.trim();
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        if (cidade == null || cidade.trim().isEmpty()) {
            throw new RegraNegocioException("A cidade do clube não pode ficar em branco.");
        }
        this.cidade = cidade.trim();
    }

    @Override
    public String exibirResumo() {
        return nome + " - " + cidade;
    }

    @Override
    public String toString() {
        return nome;
    }
}

class Campeonato implements Exibivel {
    public static final int MAX_CLUBES = 8;

    private String nome;
    private final List<Clube> clubes;
    private final List<Partida> partidas;

    public Campeonato() {
        this("Campeonato");
    }

    public Campeonato(String nome) {
        setNome(nome);
        this.clubes = new ArrayList<>();
        this.partidas = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome do campeonato não pode ficar em branco.");
        }
        this.nome = nome.trim();
    }

    public List<Clube> getClubes() {
        return clubes;
    }

    public List<Partida> getPartidas() {
        return partidas;
    }

    public void adicionarClube(Clube clube) {
        if (clube == null) {
            throw new RegraNegocioException("Selecione um clube válido.");
        }
        if (clubes.contains(clube)) {
            throw new RegraNegocioException("Esse clube já está no campeonato.");
        }
        if (clubes.size() >= MAX_CLUBES) {
            throw new RegraNegocioException("O campeonato já possui o máximo de 8 clubes.");
        }
        clubes.add(clube);
    }

    public void adicionarClube(String nome, String cidade) {
        adicionarClube(new Clube(nome, cidade));
    }

    public void adicionarPartida(Partida partida) {
        if (partida == null) {
            throw new RegraNegocioException("A partida não pode ser nula.");
        }
        partidas.add(partida);
    }

    @Override
    public String exibirResumo() {
        return nome + " | Clubes: " + clubes.size() + " | Partidas: " + partidas.size();
    }

    @Override
    public String toString() {
        return nome;
    }
}

class GrupoApostas implements Exibivel {
    private String nome;
    private final List<Participante> participantes;

    public GrupoApostas() {
        this("Grupo");
    }

    public GrupoApostas(String nome) {
        setNome(nome);
        this.participantes = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome do grupo não pode ficar em branco.");
        }
        this.nome = nome.trim();
    }

    public List<Participante> getParticipantes() {
        return participantes;
    }

    public void adicionarParticipante(Participante participante) {
        if (participante == null) {
            throw new RegraNegocioException("Selecione um participante válido.");
        }
        if (participantes.contains(participante)) {
            throw new RegraNegocioException("Esse participante já está no grupo.");
        }
        if (participante.getGrupo() != null && participante.getGrupo() != this) {
            throw new RegraNegocioException("O participante já pertence a outro grupo.");
        }
        participantes.add(participante);
        participante.setGrupo(this);
    }

    @Override
    public String exibirResumo() {
        return nome + " | Participantes: " + participantes.size();
    }

    @Override
    public String toString() {
        return nome;
    }
}

class Partida implements Exibivel {
    private final Campeonato campeonato;
    private final Clube mandante;
    private final Clube visitante;
    private final LocalDateTime dataHora;
    private Integer golsMandante;
    private Integer golsVisitante;

    public Partida(Campeonato campeonato, Clube mandante, Clube visitante, LocalDateTime dataHora) {
        this.campeonato = Objects.requireNonNull(campeonato, "Campeonato obrigatório");
        this.mandante = Objects.requireNonNull(mandante, "Mandante obrigatório");
        this.visitante = Objects.requireNonNull(visitante, "Visitante obrigatório");
        this.dataHora = Objects.requireNonNull(dataHora, "Data obrigatória");
    }

    public Campeonato getCampeonato() {
        return campeonato;
    }

    public Clube getMandante() {
        return mandante;
    }

    public Clube getVisitante() {
        return visitante;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public Integer getGolsMandante() {
        return golsMandante;
    }

    public Integer getGolsVisitante() {
        return golsVisitante;
    }

    public void registrarResultado(int golsMandante, int golsVisitante) {
        if (LocalDateTime.now().isBefore(dataHora)) {
            throw new RegraNegocioException("O resultado só pode ser registrado após o horário da partida.");
        }
        if (golsMandante < 0 || golsVisitante < 0) {
            throw new RegraNegocioException("Os gols não podem ser negativos.");
        }
        this.golsMandante = golsMandante;
        this.golsVisitante = golsVisitante;
    }

    public boolean possuiResultado() {
        return golsMandante != null && golsVisitante != null;
    }

    public boolean aceitaApostas() {
        return LocalDateTime.now().isBefore(dataHora.minusMinutes(20));
    }

    public Resultado getResultado() {
        if (!possuiResultado()) {
            return Resultado.PENDENTE;
        }
        if (golsMandante > golsVisitante) {
            return Resultado.MANDANTE;
        }
        if (golsVisitante > golsMandante) {
            return Resultado.VISITANTE;
        }
        return Resultado.EMPATE;
    }

    public String getDescricaoCurta() {
        return mandante.getNome() + " x " + visitante.getNome();
    }

    @Override
    public String exibirResumo() {
        String data = dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String resultado = possuiResultado() ? golsMandante + " x " + golsVisitante : "Sem resultado";
        return campeonato.getNome() + " | " + getDescricaoCurta() + " | " + data + " | " + resultado;
    }

    @Override
    public String toString() {
        return getDescricaoCurta() + " - " + dataHora.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }
}

class Aposta implements Exibivel, Pontuavel {
    private final Participante participante;
    private final Partida partida;
    private final int golsMandante;
    private final int golsVisitante;
    private final LocalDateTime dataRegistro;

    public Aposta(Participante participante, Partida partida, int golsMandante, int golsVisitante) {
        this.participante = Objects.requireNonNull(participante, "Participante obrigatório");
        this.partida = Objects.requireNonNull(partida, "Partida obrigatória");
        if (golsMandante < 0 || golsVisitante < 0) {
            throw new RegraNegocioException("Os gols da aposta não podem ser negativos.");
        }
        this.golsMandante = golsMandante;
        this.golsVisitante = golsVisitante;
        this.dataRegistro = LocalDateTime.now();
    }

    public Participante getParticipante() {
        return participante;
    }

    public Partida getPartida() {
        return partida;
    }

    public int getGolsMandante() {
        return golsMandante;
    }

    public int getGolsVisitante() {
        return golsVisitante;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public Resultado getResultadoPrevisto() {
        if (golsMandante > golsVisitante) {
            return Resultado.MANDANTE;
        }
        if (golsVisitante > golsMandante) {
            return Resultado.VISITANTE;
        }
        return Resultado.EMPATE;
    }

    @Override
    public int calcularPontos() {
        if (!partida.possuiResultado()) {
            return 0;
        }

        boolean acertouResultado = getResultadoPrevisto() == partida.getResultado();
        boolean acertouPlacar = golsMandante == partida.getGolsMandante() && golsVisitante == partida.getGolsVisitante();

        if (acertouResultado && acertouPlacar) {
            return 10;
        }
        if (acertouResultado) {
            return 5;
        }
        return 0;
    }

    @Override
    public String exibirResumo() {
        return participante.getNome() + " apostou " + golsMandante + " x " + golsVisitante + " em " + partida.getDescricaoCurta();
    }
}

class Sistema {
    public static final int MAX_GRUPOS = 5;
    public static final int MAX_PARTICIPANTES = 5;

    private final Administrador administrador;
    private final List<Clube> clubes;
    private final List<Campeonato> campeonatos;
    private final List<GrupoApostas> grupos;
    private final List<Participante> participantes;

    public Sistema() {
        this.administrador = new Administrador("Admin do Sistema", "admin@sistema.com");
        this.clubes = new ArrayList<>();
        this.campeonatos = new ArrayList<>();
        this.grupos = new ArrayList<>();
        this.participantes = new ArrayList<>();
    }

    public List<Clube> getClubes() {
        return clubes;
    }

    public List<Campeonato> getCampeonatos() {
        return campeonatos;
    }

    public List<GrupoApostas> getGrupos() {
        return grupos;
    }

    public List<Participante> getParticipantes() {
        return participantes;
    }

    public List<Partida> getPartidas() {
        List<Partida> partidas = new ArrayList<>();
        for (Campeonato campeonato : campeonatos) {
            partidas.addAll(campeonato.getPartidas());
        }
        return partidas;
    }

    public Clube cadastrarClube(String nome, String cidade) {
        validarNaoVazio(nome, "Informe o nome do clube.");
        validarNaoVazio(cidade, "Informe a cidade do clube.");
        if (buscarClubePorNome(nome) != null) {
            throw new RegraNegocioException("Já existe um clube com esse nome.");
        }
        Clube clube = new Clube(nome, cidade);
        clubes.add(clube);
        return clube;
    }

    public Campeonato cadastrarCampeonato(String nome) {
        validarNaoVazio(nome, "Informe o nome do campeonato.");
        if (buscarCampeonatoPorNome(nome) != null) {
            throw new RegraNegocioException("Já existe um campeonato com esse nome.");
        }
        Campeonato campeonato = new Campeonato(nome);
        campeonatos.add(campeonato);
        return campeonato;
    }

    public GrupoApostas cadastrarGrupo(String nome) {
        validarNaoVazio(nome, "Informe o nome do grupo.");
        if (grupos.size() >= MAX_GRUPOS) {
            throw new RegraNegocioException("O sistema permite no máximo 5 grupos.");
        }
        if (buscarGrupoPorNome(nome) != null) {
            throw new RegraNegocioException("Já existe um grupo com esse nome.");
        }
        GrupoApostas grupo = new GrupoApostas(nome);
        grupos.add(grupo);
        return grupo;
    }

    public Participante cadastrarParticipante(String nome, String email) {
        validarNaoVazio(nome, "Informe o nome do participante.");
        validarNaoVazio(email, "Informe o e-mail do participante.");
        if (participantes.size() >= MAX_PARTICIPANTES) {
            throw new RegraNegocioException("O sistema permite no máximo 5 participantes.");
        }
        if (buscarParticipantePorEmail(email) != null) {
            throw new RegraNegocioException("Já existe um participante com esse e-mail.");
        }
        Participante participante = new Participante(nome, email);
        participantes.add(participante);
        return participante;
    }

    public void vincularClubeAoCampeonato(Campeonato campeonato, Clube clube) {
        if (campeonato == null) {
            throw new RegraNegocioException("Selecione um campeonato.");
        }
        if (clube == null) {
            throw new RegraNegocioException("Selecione um clube.");
        }
        if (!clubes.contains(clube)) {
            throw new RegraNegocioException("O clube precisa estar cadastrado no sistema antes.");
        }
        campeonato.adicionarClube(clube);
    }

    public Partida cadastrarPartida(Campeonato campeonato, Clube mandante, Clube visitante, LocalDateTime dataHora) {
        if (campeonato == null) {
            throw new RegraNegocioException("Selecione um campeonato.");
        }
        if (mandante == null || visitante == null) {
            throw new RegraNegocioException("Selecione os dois clubes da partida.");
        }
        if (mandante.equals(visitante)) {
            throw new RegraNegocioException("Mandante e visitante devem ser diferentes.");
        }
        if (!campeonato.getClubes().contains(mandante) || !campeonato.getClubes().contains(visitante)) {
            throw new RegraNegocioException("Os clubes precisam pertencer ao campeonato selecionado.");
        }
        if (dataHora == null) {
            throw new RegraNegocioException("Informe a data da partida.");
        }
        Partida partida = new Partida(campeonato, mandante, visitante, dataHora);
        campeonato.adicionarPartida(partida);
        return partida;
    }

    public void adicionarParticipanteAoGrupo(GrupoApostas grupo, Participante participante) {
        if (grupo == null) {
            throw new RegraNegocioException("Selecione um grupo.");
        }
        if (participante == null) {
            throw new RegraNegocioException("Selecione um participante.");
        }
        grupo.adicionarParticipante(participante);
    }

    public void registrarAposta(Participante participante, Partida partida, int golsMandante, int golsVisitante) {
        if (participante == null) {
            throw new RegraNegocioException("Selecione um participante.");
        }
        if (partida == null) {
            throw new RegraNegocioException("Selecione uma partida.");
        }
        if (participante.getGrupo() == null) {
            throw new RegraNegocioException("O participante precisa estar em um grupo antes de apostar.");
        }
        if (!partida.aceitaApostas()) {
            throw new RegraNegocioException("A aposta só pode ser feita até 20 minutos antes da partida.");
        }
        boolean jaExiste = participante.getApostas().stream()
                .anyMatch(a -> a.getPartida().equals(partida));
        if (jaExiste) {
            throw new RegraNegocioException("Esse participante já apostou nessa partida.");
        }
        participante.adicionarAposta(partida, golsMandante, golsVisitante);
    }

    public void registrarResultado(Partida partida, int golsMandante, int golsVisitante) {
        if (partida == null) {
            throw new RegraNegocioException("Selecione uma partida.");
        }
        partida.registrarResultado(golsMandante, golsVisitante);
    }

    public List<Participante> classificacaoDoGrupo(GrupoApostas grupo) {
        if (grupo == null) {
            return new ArrayList<>();
        }
        return grupo.getParticipantes().stream()
                .sorted(Comparator.comparingInt(Participante::calcularPontos).reversed()
                        .thenComparing(Participante::getNome))
                .collect(Collectors.toList());
    }

    public Map<Participante, Integer> rankingComoMapa(GrupoApostas grupo) {
        Map<Participante, Integer> mapa = new LinkedHashMap<>();
        for (Participante participante : classificacaoDoGrupo(grupo)) {
            mapa.put(participante, participante.calcularPontos());
        }
        return mapa;
    }

    public void carregarDadosExemplo() {
        Clube palmeiras = cadastrarClube("Palmeiras", "São Paulo");
        Clube santos = cadastrarClube("Santos", "Santos");
        Clube corinthians = cadastrarClube("Corinthians", "São Paulo");
        Clube spfc = cadastrarClube("São Paulo", "São Paulo");

        Campeonato campeonato = cadastrarCampeonato("Paulistão Demo");
        vincularClubeAoCampeonato(campeonato, palmeiras);
        vincularClubeAoCampeonato(campeonato, santos);
        vincularClubeAoCampeonato(campeonato, corinthians);
        vincularClubeAoCampeonato(campeonato, spfc);

        Participante ana = cadastrarParticipante("Ana", "ana@email.com");
        Participante bruno = cadastrarParticipante("Bruno", "bruno@email.com");
        Participante carla = cadastrarParticipante("Carla", "carla@email.com");

        GrupoApostas grupo = cadastrarGrupo("Grupo Principal");
        adicionarParticipanteAoGrupo(grupo, ana);
        adicionarParticipanteAoGrupo(grupo, bruno);
        adicionarParticipanteAoGrupo(grupo, carla);

        Partida futura = cadastrarPartida(campeonato, palmeiras, santos, LocalDateTime.now().plusDays(2));
        Partida encerrada = cadastrarPartida(campeonato, corinthians, spfc, LocalDateTime.now().minusDays(1));

        registrarAposta(ana, futura, 2, 1);
        registrarAposta(bruno, futura, 1, 1);
        registrarAposta(carla, futura, 0, 1);

        ana.adicionarAposta(encerrada, 1, 0);
        bruno.adicionarAposta(encerrada, 2, 1);
        carla.adicionarAposta(encerrada, 0, 2);
        registrarResultado(encerrada, 1, 0);
    }

    private Clube buscarClubePorNome(String nome) {
        return clubes.stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome.trim()))
                .findFirst()
                .orElse(null);
    }

    private Campeonato buscarCampeonatoPorNome(String nome) {
        return campeonatos.stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome.trim()))
                .findFirst()
                .orElse(null);
    }

    private GrupoApostas buscarGrupoPorNome(String nome) {
        return grupos.stream()
                .filter(g -> g.getNome().equalsIgnoreCase(nome.trim()))
                .findFirst()
                .orElse(null);
    }

    private Participante buscarParticipantePorEmail(String email) {
        return participantes.stream()
                .filter(p -> p.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst()
                .orElse(null);
    }

    private void validarNaoVazio(String valor, String mensagem) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new RegraNegocioException(mensagem);
        }
    }
}

enum Resultado {
    MANDANTE,
    VISITANTE,
    EMPATE,
    PENDENTE
}

class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String message) {
        super(message);
    }
}
