package com.finance.voiceai.tool;

import com.finance.voiceai.domain.dto.CategoryBudgetStatus;
import com.finance.voiceai.domain.dto.FinancialSummaryDto;
import com.finance.voiceai.domain.entity.Transaction;
import com.finance.voiceai.service.FinanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Component
public class FinanceTools {

    private static final Logger log = LoggerFactory.getLogger(FinanceTools.class);
    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final ThreadLocal<List<String>> INVOKED_TOOLS = ThreadLocal.withInitial(ArrayList::new);

    private final FinanceService financeService;

    public FinanceTools(FinanceService financeService) {
        this.financeService = financeService;
    }

    public static void clearInvokedTools() {
        INVOKED_TOOLS.get().clear();
    }

    public static List<String> getInvokedTools() {
        return Collections.unmodifiableList(new ArrayList<>(INVOKED_TOOLS.get()));
    }

    private static void recordTool(String toolNameWithDetails) {
        INVOKED_TOOLS.get().add(toolNameWithDetails);
        log.info("[Spring AI Tool Calling] Executado: {}", toolNameWithDetails);
    }

    private String formatMoney(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(amount);
    }

    @Tool(description = "Registra uma nova despesa ou gasto no orçamento financeiro, informando a descrição, o valor monetário em reais, a categoria (ex: Alimentação, Transporte, Lazer, Moradia, Saúde) e observações opcionais.")
    public String registrarDespesa(String descricao, double valor, String categoria, String observacoes) {
        recordTool("registrarDespesa(descricao='" + descricao + "', valor=" + valor + ", categoria='" + categoria + "')");
        
        BigDecimal amount = BigDecimal.valueOf(valor);
        Transaction tx = financeService.registerExpense(descricao, amount, categoria, observacoes);

        var budgetOpt = financeService.getCategoryBudget(tx.getCategory().getName());
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Despesa '%s' no valor de %s registrada com sucesso na categoria '%s'. ",
                tx.getDescription(), formatMoney(tx.getAmount()), tx.getCategory().getName()));

        budgetOpt.ifPresent(b -> {
            if (b.isOverBudget()) {
                sb.append(String.format("⚠️ ATENÇÃO: O limite da categoria %s (%s) foi ultrapassado em %s! Total gasto: %s.",
                        b.getCategoryName(), formatMoney(b.getMonthlyLimit()),
                        formatMoney(b.getCurrentSpent().subtract(b.getMonthlyLimit())),
                        formatMoney(b.getCurrentSpent())));
            } else if (b.getPercentageUsed() >= 80.0) {
                sb.append(String.format("Alerta: Você já utilizou %.1f%% do limite da categoria %s. Restam apenas %s.",
                        b.getPercentageUsed(), b.getCategoryName(), formatMoney(b.getRemaining())));
            } else {
                sb.append(String.format("Situação da categoria: Gasto acumulado de %s de um limite de %s (restam %s).",
                        formatMoney(b.getCurrentSpent()), formatMoney(b.getMonthlyLimit()), formatMoney(b.getRemaining())));
            }
        });

        return sb.toString();
    }

    @Tool(description = "Registra uma nova receita, salário ou entrada financeira informando descrição, valor em reais e categoria.")
    public String registrarReceita(String descricao, double valor, String categoria, String observacoes) {
        recordTool("registrarReceita(descricao='" + descricao + "', valor=" + valor + ", categoria='" + categoria + "')");
        
        BigDecimal amount = BigDecimal.valueOf(valor);
        Transaction tx = financeService.registerIncome(descricao, amount, categoria, observacoes);
        FinancialSummaryDto summary = financeService.getSummary();

        return String.format("Receita '%s' no valor de %s registrada com sucesso na categoria '%s'. O novo saldo consolidado é %s.",
                tx.getDescription(), formatMoney(tx.getAmount()), tx.getCategory().getName(), formatMoney(summary.getNetBalance()));
    }

    @Tool(description = "Consulta o saldo atual consolidado do usuário, informando total de receitas, total de despesas e saldo líquido disponível.")
    public String consultarSaldo() {
        recordTool("consultarSaldo()");
        
        FinancialSummaryDto summary = financeService.getSummary();
        return String.format("Resumo Financeiro Atual:\n- Total de Receitas: %s\n- Total de Despesas: %s\n- Saldo Líquido: %s.",
                formatMoney(summary.getTotalIncome()),
                formatMoney(summary.getTotalExpense()),
                formatMoney(summary.getNetBalance()));
    }

    @Tool(description = "Consulta o status do orçamento de uma categoria específica (ex: Alimentação, Lazer) ou de todas as categorias se vazio. Retorna limite, quanto já gastou e quanto ainda resta.")
    public String consultarOrcamento(String categoria) {
        recordTool("consultarOrcamento(categoria='" + categoria + "')");
        
        if (categoria != null && !categoria.trim().isEmpty()) {
            var opt = financeService.getCategoryBudget(categoria);
            if (opt.isPresent()) {
                CategoryBudgetStatus b = opt.get();
                if (b.isOverBudget()) {
                    return String.format("Categoria %s: Limite mensal de %s. Já foram gastos %s (ultrapassou em %s, consumo de %.1f%%).",
                            b.getCategoryName(), formatMoney(b.getMonthlyLimit()), formatMoney(b.getCurrentSpent()),
                            formatMoney(b.getCurrentSpent().subtract(b.getMonthlyLimit())), b.getPercentageUsed());
                }
                return String.format("Categoria %s: Limite mensal de %s. Já foram gastos %s. Saldo restante disponível: %s (%.1f%% utilizado).",
                        b.getCategoryName(), formatMoney(b.getMonthlyLimit()), formatMoney(b.getCurrentSpent()),
                        formatMoney(b.getRemaining()), b.getPercentageUsed());
            }
        }

        List<CategoryBudgetStatus> all = financeService.getAllCategoryBudgets();
        StringBuilder sb = new StringBuilder("Status de todas as categorias de orçamento:\n");
        for (CategoryBudgetStatus b : all) {
            String alerta = b.isOverBudget() ? " ⚠️ ESTOURADO" : (b.getPercentageUsed() >= 80 ? " ⚠️ ATENÇÃO" : "");
            sb.append(String.format("• %s %s: Limite %s | Gasto %s | Restante %s (%.1f%%)%s\n",
                    b.getCategoryIcon(), b.getCategoryName(), formatMoney(b.getMonthlyLimit()),
                    formatMoney(b.getCurrentSpent()), formatMoney(b.getRemaining()), b.getPercentageUsed(), alerta));
        }
        return sb.toString();
    }

    @Tool(description = "Lista as transações e movimentações financeiras mais recentes do usuário.")
    public String listarTransacoesRecentes(Integer limite) {
        int count = (limite != null && limite > 0) ? limite : 5;
        recordTool("listarTransacoesRecentes(limite=" + count + ")");
        
        var recent = financeService.getRecentTransactions(count);
        if (recent.isEmpty()) {
            return "Nenhuma transação registrada até o momento.";
        }
        StringBuilder sb = new StringBuilder("Últimas movimentações registradas:\n");
        for (var t : recent) {
            String prefix = t.getType().name().equals("INCOME") ? "(+) " : "(-) ";
            sb.append(String.format("• %s%s %s: %s [%s]\n",
                    prefix, t.getCategoryIcon(), t.getDescription(), formatMoney(t.getAmount()), t.getCategoryName()));
        }
        return sb.toString();
    }

    @Tool(description = "Gera um relatório financeiro completo com visão geral de receitas, despesas, saldo e alertas de categorias críticas.")
    public String obterRelatorioGeral() {
        recordTool("obterRelatorioGeral()");
        
        FinancialSummaryDto summary = financeService.getSummary();
        StringBuilder sb = new StringBuilder("=== RELATÓRIO FINANCEIRO GERAL ===\n");
        sb.append(String.format("Saldo em Conta: %s\nReceitas Totais: %s\nDespesas Totais: %s\n\n",
                formatMoney(summary.getNetBalance()), formatMoney(summary.getTotalIncome()), formatMoney(summary.getTotalExpense())));

        List<CategoryBudgetStatus> criticas = summary.getCategoryBudgets().stream()
                .filter(c -> c.isOverBudget() || c.getPercentageUsed() >= 80.0)
                .toList();

        if (!criticas.isEmpty()) {
            sb.append("⚠️ CATEGORIAS EM ALERTA:\n");
            for (CategoryBudgetStatus c : criticas) {
                sb.append(String.format("• %s: gasto de %s de %s (%.1f%% consumido)%s\n",
                        c.getCategoryName(), formatMoney(c.getCurrentSpent()), formatMoney(c.getMonthlyLimit()),
                        c.getPercentageUsed(), c.isOverBudget() ? " [LIMITE ULTRAPASSADO!]" : ""));
            }
        } else {
            sb.append("✅ Todas as categorias estão dentro dos limites estipulados!\n");
        }
        return sb.toString();
    }
}
