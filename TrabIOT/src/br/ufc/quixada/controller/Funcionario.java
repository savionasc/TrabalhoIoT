package br.ufc.quixada.controller;

import java.util.ArrayList;

public class Funcionario {
	public String nome;
	public boolean ocupado;
	public String quarto;
	public ArrayList<String> filaQuartos = null;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public boolean isOcupado() {
		return ocupado;
	}

	public void setOcupado(boolean ocupado) {
		this.ocupado = ocupado;
	}

	public String getQuarto() {
		return quarto;
	}

	public void setQuarto(String quarto) {
		this.quarto = quarto;
	}

	public ArrayList<String> getFilaQuartos() {
		return filaQuartos;
	}

	public void setFilaQuartos(ArrayList<String> filaQuartos) {
		this.filaQuartos = filaQuartos;
	}

	public Funcionario(String nome) {
		super();
		this.nome = nome;
		this.ocupado = false;
		this.filaQuartos = new ArrayList<String>();
	}
	
	public void addOcupacao(String quarto){
		this.quarto = quarto;
		this.ocupado = true;
	}
	
	public String removeOcupacao(){
		String retorno = this.quarto;
		this.quarto = "";
		this.ocupado = false;
		return retorno;
	}
	
	public void addQuartoNaFila(String quarto){
		this.filaQuartos.add(quarto);
	}
	
	public void retirarDaEspera(String quarto){
		if(filaQuartos.size() > 0){
			for (int i = 0; i < filaQuartos.size(); i++) {
				if(filaQuartos.get(i).equals(quarto)){
					this.ocupado = true;
					this.quarto = filaQuartos.remove(i);					
				}
			}
		}
	}
	
	public void removeQuartoNaFila(String quarto){
		for (int i = 0; i < filaQuartos.size(); i++) {
			if(filaQuartos.get(i).equals(quarto)){
				filaQuartos.remove(i);
			}
		}
	}
}
