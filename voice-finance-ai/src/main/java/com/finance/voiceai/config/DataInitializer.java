package com.finance.voiceai.config;

import com.finance.voiceai.domain.entity.Category;
import com.finance.voiceai.domain.entity.Transaction;
import com.finance.voiceai.domain.entity.TransactionType;
import com.finance.voiceai.repository.CategoryRepository;
import com.finance.voiceai.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public DataInitializer(CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            log.info("[DataInitializer] Inicializando categorias e dados de exemplo...");

            Category alimentacao = categoryRepository.save(new Category("Alimentação", BigDecimal.valueOf(1200.00), "🍔", "#F59E0B"));
            Category transporte = categoryRepository.save(new Category("Transporte", BigDecimal.valueOf(500.00), "🚗", "#3B82F6"));
            Category moradia = categoryRepository.save(new Category("Moradia", BigDecimal.valueOf(2000.00), "🏠", "#10B981"));
            Category lazer = categoryRepository.save(new Category("Lazer", BigDecimal.valueOf(400.00), "🎮", "#EC4899"));
            Category saude = categoryRepository.save(new Category("Saúde", BigDecimal.valueOf(350.00), "💊", "#EF4444"));
            Category salario = categoryRepository.save(new Category("Salário", BigDecimal.ZERO, "💵", "#059669"));
            Category outros = categoryRepository.save(new Category("Outros", BigDecimal.valueOf(300.00), "📦", "#8B5CF6"));

            // Transações iniciais
            transactionRepository.saveAll(List.of(
                    new Transaction("Salário Mensal", BigDecimal.valueOf(4800.00), TransactionType.INCOME, salario, LocalDateTime.now().minusDays(5), "Depósito em conta corrente"),
                    new Transaction("Supermercado Semanal", BigDecimal.valueOf(385.50), TransactionType.EXPENSE, alimentacao, LocalDateTime.now().minusDays(4), "Compras de mantimentos"),
                    new Transaction("Combustível Posto Ipiranga", BigDecimal.valueOf(140.00), TransactionType.EXPENSE, transporte, LocalDateTime.now().minusDays(3), "Gasolina aditivada"),
                    new Transaction("Ingresso Cinema e Lanche", BigDecimal.valueOf(65.00), TransactionType.EXPENSE, lazer, LocalDateTime.now().minusDays(1), "Final de semana"),
                    new Transaction("Farmácia São Paulo", BigDecimal.valueOf(42.90), TransactionType.EXPENSE, saude, LocalDateTime.now().minusHours(12), "Vitaminas")
            ));

            log.info("[DataInitializer] Categorias e transações iniciais carregadas com sucesso!");
        }
    }
}
