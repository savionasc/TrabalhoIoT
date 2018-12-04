package br.ufc.quixada.controller;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.lang.Thread;

@Transactional
@Controller
public class PrincipalController {

	@Autowired
	private ServletContext servletContext;
	
	static List<String> cinemaria = new ArrayList<String>();
	static List<Integer[]> distancias = new ArrayList<Integer[]>();
		
	static ArrayList<Funcionario> funcOcupados = new ArrayList<Funcionario>();
	static ArrayList<Funcionario> funcDesocupados = new ArrayList<Funcionario>();
	static ArrayList<String> quartosAtivos = new ArrayList<String>();
	static ArrayList<String> filaEspera = new ArrayList<String>();
	static Publisher p;

	//Construtor da aplicação
	static void construtor(){
		cinemaria.add("beatriz");
		distancias.add(new Integer[]{0, 1, 2, 3, 4});
		cinemaria.add("jesca");
		distancias.add(new Integer[]{1, 0, 1, 2, 3});
		cinemaria.add("segundo");
		distancias.add(new Integer[]{2, 1, 0, 1, 2});
		cinemaria.add("vinicius");
		distancias.add(new Integer[]{3, 2, 1, 0, 1});
		cinemaria.add("yuri");
		distancias.add(new Integer[]{4, 3, 2, 1, 0});
	}
	
	static boolean contemNaLista(ArrayList<Funcionario> lista, String nome){
		for (Funcionario funcionario : lista) {
			if(funcionario.nome.equals(nome)){
				return true;
			}
		}
		return false;
	}
	
	//Quando o func fizer login no app
	static void addFuncionario(String nome){
		if(!contemNaLista(funcDesocupados,nome) && !contemNaLista(funcOcupados,nome)){
			Funcionario f = new Funcionario(nome);		
			funcDesocupados.add(f);
			p = new Publisher("mobile", "funcionario_aceito/"+nome);
		}else{
			p = new Publisher("mobile", "funcionario_recusado/"+nome);
		}
		p.run();			
	}

	//Quando o func fizer logout no app
	static void removerFuncionario(String nome){
		p = new Publisher("mobile", "funcionario_nao_encontrado");
		if(contemNaLista(funcDesocupados, nome)){
			for (int i = 0; i < funcDesocupados.size(); i++) {
				if(((Funcionario)funcDesocupados.get(i)).nome.equals(nome)){
					funcDesocupados.remove(i);
					p = new Publisher("mobile", "funcionario_retirado");
				}
			}
		}else if(contemNaLista(funcOcupados, nome)){
			for (int i = 0; i < funcOcupados.size(); i++) {
				if(((Funcionario)funcOcupados.get(i)).nome.equals(nome)){
					funcOcupados.remove(i);
					p = new Publisher("mobile", "funcionario_retirado");
				}				
			}
		}
		p.run();
	}
	
	//Quando solicitarem um func para limpar um quarto
	static void addOcupacao(String quarto){
		//Se houver funcionario desocupado
		if(funcDesocupados.size() > 0){
			Funcionario f = funcDesocupados.remove(0);
			f.addOcupacao(quarto);
			funcOcupados.add(f);
			System.out.println("Funcionario chamado: "+f.nome);
			quartosAtivos.add(quarto);
		}else{
			p = new Publisher("arduino", "nenhum_funcionario_desocupado");
			p.run();
		}
	}
	
	static void avaliarChamado(String avaliacao){
		p = new Publisher("mobile", "avaliado_chamado/"+avaliacao);
		p.run();
		System.out.println("Chamado avaliado como: "+avaliacao);
		//p = new Publisher("arduino", "avaliado_chamado/"+avaliacao);
		//p.run();		
	}
	
	static void confirmarChamado(String quarto){
		if(!filaEspera.contains(quarto)){
			p = new Publisher("mobile", "chamado_desconhecido/"+quarto);
			p.run();
			return;
		}
		
		for (Funcionario f : funcOcupados) {
			if(f.quarto.equals(quarto)){
				p = new Publisher("mobile", "chamado_em_andamento/"+quarto);
				p.run();
				return;
			}
		}
		addOcupacao(quarto);
		p = new Publisher("mobile", "confirmado_chamado/"+quarto);
		p.run();
		p = new Publisher("arduino", "confirmado_chamado/"+quarto);
		p.run();
	}
	static int adicionarQuartoMaisProximo(String quarto){
		//Se houver funcionarios ocupados
//		if(funcDesocupados.size() == 0){
		int idMelhor = -1;
		int distancia = 100;
		for (int i = 0; i < funcOcupados.size(); i++) {
			if(i != idMelhor){
				String quartoAtual = funcOcupados.get(i).quarto;
				int idQuartoAtual = cinemaria.indexOf(quartoAtual);
				int idQuartoDesejado = cinemaria.indexOf(quarto);
				int dist = distancias.get(idQuartoAtual)[idQuartoDesejado];
				if(dist < distancia){
					distancia = dist;
					idMelhor = i;
				}
			}
		}
		
		if(idMelhor != -1){
			Funcionario f = funcOcupados.remove(idMelhor);
			if(!f.filaQuartos.contains(quarto)){
				f.addQuartoNaFila(quarto);
				funcOcupados.add(f);
				filaEspera.remove(quarto);
				System.out.println("Func adicionado; Funcionario chamado: "+f.nome+" id: "+idMelhor+" Dist: "+distancia);
			}
		}else if(funcDesocupados.size() > 0){
			Funcionario f = funcDesocupados.remove(0);
			f.addQuartoNaFila(quarto);
			funcDesocupados.add(f);
			System.out.println("Func adicionado; Funcionario chamado: "+f.nome);			
			
		}
		return 0;
	}

	static void desocupar(String quarto){
		boolean ativo = false;
		//Funcionario fAux = new Funcionario("AuxAuxAux");
		for (int i = 0; i < funcOcupados.size(); i++) {
			Funcionario f = funcOcupados.get(i);
			if(f.quarto.equals(quarto)){
				ativo = true;
				f = funcOcupados.remove(i);
				f.removeOcupacao();
				System.out.println("o F: "+f.nome+" desocupou o q: "+quarto);
				p = new Publisher("mobile", "chamado_finalizado/"+quarto);
				p.run();
				p = new Publisher("arduino", "chamado_finalizado/"+quarto);
				p.run();
				if(f.filaQuartos.size() == 0){
					funcDesocupados.add(f);					
				}else{
					funcOcupados.add(f);
					//fAux = f;
				}
			}
		}
		/*if(!fAux.nome.equals("AuxAuxAux")){
			confirmarChamado(fAux.quarto);			
		}*/

		if(!ativo){
			p = new Publisher("mobile", "chamado_desconhecido/"+quarto);
			p.run();
		}
		
		quartosAtivos.remove(quarto);
	}

	static void listarFuncionarios(){
		System.out.println("Funcionários:");
		for (int i = 0; i < funcDesocupados.size(); i++) {
			Funcionario f = ((Funcionario)funcDesocupados.get(i));
			System.out.println(f.nome+" está desocupado");
		}
		
		for (int i = 0; i < funcOcupados.size(); i++) {
			Funcionario f = ((Funcionario)funcOcupados.get(i));
			System.out.println(f.nome+" está no quarto: "+f.quarto);
		}
	}

	@RequestMapping("/mostrarMensagem")
	public String mostrarMensagem(Model model, HttpSession session){
		System.out.println("Passou aqui!");
		return "teste";
	}

	@RequestMapping("/listarFuncionarios")
	public String listarFuncionários(Model model, HttpSession session){
		System.out.println("lista de funcionários.");
		model.addAttribute("filaEspera", filaEspera);
		model.addAttribute("funcDesocupados", funcDesocupados);
		model.addAttribute("funcOcupados", funcOcupados);
		return "listarFuncionarios";
	}

	static MqttClient servidor;
	static Thread t = new Thread();
	static Runnable ruunn = new Runnable() {
		@Override
		public void run() {
			try {
		        String broker   = "tcp://localhost:1883";
		        String clientId = "idServidor";
		        MemoryPersistence persistence = new MemoryPersistence();
		        MqttConnectOptions connOpts = new MqttConnectOptions();
				connOpts.setUserName("savio_u_mqtt");
				connOpts.setPassword("mqtt_senha".toCharArray());
				connOpts.setCleanSession(true);
		        
	        	MqttCallback mCallback = new MqttCallback() {
	        	    @Override
	        	    public void connectionLost(Throwable cause) {
	        	      // Do nothing...
	        	    }

	        	    @Override
	        	    public void messageArrived(String topic, MqttMessage message) throws Exception {
	        	    	String payload = new String(message.getPayload());
						System.out.println("Recebi: "+payload);
						if(payload.startsWith("arduino/solicitar_chamado/")){
							String quarto = payload.substring("arduino/solicitar_chamado/".length());
							if(!quartosAtivos.contains(quarto) || !filaEspera.contains(quarto)){
								filaEspera.add(quarto);
								p = new Publisher("arduino", "chamado_agendado");
								System.out.println("arduino: chamado_agendado");
								p.run(); 
								p = new Publisher("mobile", "solicitar_chamado/"+quarto);
								p.run();
							}else{
								p = new Publisher("arduino", "quarto_ja_cadastrado");
								p.run();
							}
							System.out.println("d");
						}else if(payload.startsWith("mobile/encaminhar_chamado/")){
							String quarto = payload.substring("mobile/encaminhar_chamado/".length());
							if(funcOcupados.size() > 0 || funcDesocupados.size() > 0){
								adicionarQuartoMaisProximo(quarto);			
								p = new Publisher("arduino", "chamado_espera");
								//chamado_espera. Quarto: "+quarto
							}else{
								p = new Publisher("arduino", "nenhum_funcionario");
							}
							p.run();
						}else if(payload.startsWith("mobile/confirmar_chamado/")){
							String quarto = payload.substring("mobile/confirmar_chamado/".length());
							if(funcOcupados.size() > 0){
								quartosAtivos.add(quarto);
								confirmarChamado(quarto);

								for (int i = 0; i < funcOcupados.size(); i++) {
									if(funcOcupados.get(i).filaQuartos.contains(quarto)){
										funcOcupados.get(i).retirarDaEspera(quarto);
									}
								}

								for (int i = 0; i < filaEspera.size(); i++) {
									if(filaEspera.get(i).equals(quarto)){
										filaEspera.remove(quarto);
									}
								}
							}else if(funcDesocupados.size() > 0){
								quartosAtivos.add(quarto);
								confirmarChamado(quarto);

								for (int i = 0; i < funcOcupados.size(); i++) {
									if(funcOcupados.get(i).filaQuartos.contains(quarto)){
										funcOcupados.get(i).retirarDaEspera(quarto);
									}
								}

								for (int i = 0; i < filaEspera.size(); i++) {
									if(filaEspera.get(i).equals(quarto)){
										filaEspera.remove(quarto);
									}
								}								
							}
							
							//p = new Publisher("arduino", "chamado_solicitado/"+quarto);
							//p.run();
							//p = new Publisher("mobile", "chamado_solicitado/"+quarto);			

							
						}else if(payload.startsWith("arduino/avaliar_chamado/")){
							String avaliacao = payload.substring("arduino/avaliar_chamado/".length());
							avaliarChamado(avaliacao);
						}else if(payload.startsWith("mobile/finalizar_chamado/")){
							String quarto = payload.substring("mobile/finalizar_chamado/".length());
							desocupar(quarto);
						}else if(payload.startsWith("mobile/login/")){
							String usuario = payload.substring("mobile/login/".length());
							addFuncionario(usuario);
						}else if(payload.startsWith("mobile/logout/")){
							String usuario = payload.substring("mobile/logout/".length());
							removerFuncionario(usuario);
						}else if(payload.equals("listar/funcionarios")){
							listarFuncionarios();
						}
	        	    }

					@Override
					public void deliveryComplete(IMqttDeliveryToken arg0) {
						// TODO Auto-generated method stub
					}
	        	};

	        	servidor = new MqttClient(broker, clientId, persistence);
	            System.out.println("Connecting to broker: "+broker);
	            servidor.connect(connOpts);
	            servidor.subscribe("servidor", 1);
	            servidor.setCallback(mCallback);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	public static void main(String[] args) {
		if(!t.isAlive()){
			construtor();
			t = new Thread(ruunn);
			t.start();
			System.out.println("Iniciou nova Thread");
		}
	}
}
