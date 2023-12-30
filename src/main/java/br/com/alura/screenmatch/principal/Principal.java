package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSeries = new ArrayList<>();

    private SerieRepository serieRepository;
    private List<Serie> series = new ArrayList<>();

    public Principal(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public void exibeMenu() {
        int opcao = -1;

        while (opcao != 0) {
            var menu = """
            1 - Buscar séries
            2 - Buscar episódios
            3 - Listar séries buscadas
            4-  Buscar série por titulo
            5- Buscar por genêro
            6- Buscar port ator
            0 - Sair                                
            """;

            System.out.println(menu);

            if (leitura.hasNextInt()) {
                opcao = leitura.nextInt();
                leitura.nextLine();
            } else {
                System.out.println("Por favor, digite um número inteiro válido.");
                leitura.nextLine();
                continue;
            }

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscaPorGenero();
                    break;
                case 6:
                    buscaPorAtor();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        //dadosSeries.add(dados);
        Serie serie = new Serie(dados);
        serieRepository.save(serie);
        System.out.println(dados);
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Digite o nome da série que você deseja ver os episódios:");
        var nomeSerie = leitura.nextLine().toUpperCase();

        //Buscando os episódios pelo nome da série
        Optional<Serie> serie = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);
        if (serie.isPresent()){
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();
            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .toList();
            serieEncontrada.setEpisodios(episodios);
            serieRepository.save(serieEncontrada);
        }else {
            System.out.println("Série não encontrada");
        }
    }

    private void listarSeriesBuscadas(){
        series = serieRepository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        return conversor.obterDados(json, DadosSerie.class);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Digite o titulo da série:");
        var nomeSerie = leitura.nextLine().toUpperCase();
        Optional<Serie>serieBuscada = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBuscada.isPresent()){
            System.out.println("Dados da série: " + serieBuscada.get());
        }else{
            System.out.println("Série não encontrada");
        }
    }

    private void buscaPorGenero() {
        System.out.println("Digite o gênero que você gosta:");
        var genero = leitura.nextLine().toUpperCase();

        List<Serie> seriesPorGenero = serieRepository.findByGenero(Categoria.valueOf(genero));
        if (!seriesPorGenero.isEmpty()) {
            System.out.println("Dados das séries:");
            for (Serie serie : seriesPorGenero) {
                System.out.println(serie);
            }
        } else {
            System.out.println("Gênero não encontrado");
        }
    }

    public void buscaPorAtor(){
        System.out.println("Digite o nome do ator que você gosta:");
        var ator = leitura.nextLine().toUpperCase();

        List<Serie> seriesPorAtor = serieRepository.findByAtoresContainingIgnoreCase(ator);
        if (!seriesPorAtor.isEmpty()){
            System.out.println("Séries que o Ator trabalhou em :");
            for (Serie serie : seriesPorAtor) {
                System.out.println(serie.getTitulo());
            }
        }else {
            System.out.println("Ator não encontrado(a)");
        }
    }
}