package br.com.cod3r.calc.modelo;

import java.util.ArrayList;
import java.util.List;

public class Memoria {
	
	private enum TipoComando {
		ZERAR, NUMERO, DIV, MULT, SUB, SOMA, IGUAL, VIRGULA, PLUSMINUS;
	};
	
	private static final Memoria instancia = new Memoria();

	private TipoComando ultimaAlteracao = null;
	private boolean substituir = false;
	private String textoAtual = "";
	private String textoBuffer = "";
	
	private final List<MemoriaObservador> observadores = new ArrayList<>();
	
	private Memoria() {
		
	}
	
	public static Memoria getInstancia() {
		return instancia;
	}
	
	public void adicionarObservador(MemoriaObservador observador) {
		observadores.add(observador);
	}

	public String getTextoAtual() {
		return textoAtual.isEmpty() ? "0": textoAtual;
	}
	
	public void processarCamando(String valor) {
		
		TipoComando tipoComando = detectarTipoComando(valor);

		if (tipoComando == null) {
			return;
		} else if (tipoComando == TipoComando.ZERAR) {
			textoAtual = "";
			textoBuffer = "";
			substituir = false;
			ultimaAlteracao = null;
		} else if (tipoComando == TipoComando.NUMERO 
				   || tipoComando == TipoComando.VIRGULA) {
			textoAtual = substituir ? valor : textoAtual + valor;
			substituir = false;
		} else if (tipoComando == TipoComando.PLUSMINUS) {
			ultimaAlteracao = tipoComando;
			textoAtual = obterResultadoOperacao();
			ultimaAlteracao = null;
		} else {
			substituir = true;
			textoAtual = obterResultadoOperacao();
			textoBuffer = textoAtual;
			ultimaAlteracao = tipoComando;
		}
		
		observadores.forEach(o -> o.valorAlterado(textoAtual));
	}

	private String obterResultadoOperacao() {
		if (ultimaAlteracao == null || ultimaAlteracao == TipoComando.IGUAL) {
			return textoAtual;
		}
		
		double numeroBuffer = textoBuffer.isEmpty() ? 0 : Double.parseDouble(textoBuffer.replace(",", "."));
		double numeroAtual = Double.parseDouble(textoAtual.replace(",", "."));
		double resultado = 0;
		
		if (ultimaAlteracao == TipoComando.SOMA) {
			resultado = numeroBuffer + numeroAtual;
		} else if (ultimaAlteracao == TipoComando.SUB) {
			resultado = numeroBuffer - numeroAtual;
		} else if (ultimaAlteracao == TipoComando.MULT) {
			resultado = numeroBuffer * numeroAtual;
		} else if (ultimaAlteracao == TipoComando.DIV) {
			resultado = numeroBuffer / numeroAtual;
		} else if (ultimaAlteracao == TipoComando.PLUSMINUS) {
			resultado = numeroAtual * -1;
		}
		
		String resultadoString = Double.toString(resultado).replace(".", ",");
		boolean inteiro = resultadoString.endsWith(",0");
		return inteiro ? resultadoString.replace(",0", ""): resultadoString;
	}

	private TipoComando detectarTipoComando(String valor) {
		if (textoAtual.isEmpty() && valor == "0") {
			return null;
		}
		
		try {
			Integer.parseInt(valor);
			return TipoComando.NUMERO;
		} catch (NumberFormatException e) {
			// Quando não for numero ....
			if ("AC".equals(valor)) {
				return TipoComando.ZERAR;
			} else if ("/".equals(valor)) {
				return TipoComando.DIV;
			} else if ("*".equals(valor)) {
				return TipoComando.MULT;
			} else if ("+".equals(valor)) {
				return TipoComando.SOMA;
			} else if ("-".equals(valor)) {
				return TipoComando.SUB;
			} else if ("=".equals(valor)) {
				return TipoComando.IGUAL;
			} else if (",".equals(valor) &&
					   !textoAtual.contains(",")) {
				return TipoComando.VIRGULA;
			} else if ("±".equals(valor)) {
				return TipoComando.PLUSMINUS;
			}
			
		}
		
		return null;
	}

	
}
