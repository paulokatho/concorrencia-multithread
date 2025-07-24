package br.com.katho.processamento_multithread.pedido;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    // Processamento Sequencial
    public void processarSequencial(List<Long> id) {
        List<Pedido> pedidos = pedidoRepository.findAllById(id);
        pedidos.forEach( pedido -> processarPedido(pedido));

        pedidoRepository.saveAll(pedidos);
    }

    // Processmento Paralelo
    public void processarParalelo(List<Long> id) {
        List<Pedido> pedidos = pedidoRepository.findAllById(id);
        pedidos.parallelStream().forEach(this::processarPedido);

        pedidoRepository.saveAll(pedidos);
    }

    //Processamento em Lotes
    public void processaremLottes(List<Long> id, int tamanhoDoLote) {
        List<List<Long>> lotes = dividirEmLotes(id, tamanhoDoLote);

        List<CompletableFuture<Void>> futures = lotes.stream()
                .map(lote -> CompletableFuture.runAsync(() -> {
                    List<Pedido> pedidos = pedidoRepository.findAllById(lote);
                    for(Pedido pedido : pedidos) {
                        processarPedido(pedido);
                    }
                    pedidoRepository.saveAll(pedidos);
                }))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    // Metodo auxiliar: processar um pedido individual
    private void processarPedido(Pedido pedido) {
        try {
            // Simula o tempo de processamento
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pedido.setAprovado(true);
    }

    // Metodo auxiliar: dividir lista em lotes
    private List<List<Long>> dividirEmLotes(List<Long> ids, int tamanhoDoLote) {
        List<List<Long>> lotes = new ArrayList<>();
        for(int i = 0; i < ids.size(); i += tamanhoDoLote) {
            int fim = Math.min(i + tamanhoDoLote, ids.size());
            lotes.add(ids.subList(i, fim));
        }
        return lotes;
    }
}
