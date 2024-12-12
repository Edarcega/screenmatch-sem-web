package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.services.ConsumoAPI;
import br.com.alura.screenmatch.services.ConverterDados;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
public class Principal {
    Scanner sc = new Scanner(System.in);

    @Autowired
    private ConsumoAPI consumoAPI;

    @Autowired
    private ConverterDados converterDados;

    public final String ENDERECO = "https://www.omdbapi.com/?t=";

    public final String API_KEY = "&apikey=6585022c";

    public void exibeMenu() {
        System.out.println("Digite o nome da série para buscar");
        var nomeSerie = sc.nextLine();
        var json = consumoAPI.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dadosSerie = converterDados.obterDados(json, DadosSerie.class);
        System.out.println(dadosSerie);

        List<DadosTemporada> temporadas = new ArrayList<>();
        for (int i = 1; i <= dadosSerie.totalTemporadas(); i++) {
            json = consumoAPI.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = converterDados.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);
        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())// Dentro de uma lista, gera outra lista
                .collect(Collectors.toList());// Gera uma lista que pode ser alterada

        System.out.println("Top 5 episódios");
        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A")) // Filtrando por um atributo especifico
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed()) // Comparando pelo parametro de avaliação e ordenando do maior para o menor
                .limit(5) // Limitando a 5 retornos
                .forEach(System.out::println); // Imprimindo na tela

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());

        episodios.forEach(System.out::println);
    }


}
