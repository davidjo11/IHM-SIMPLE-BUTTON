package e201.skeleton ;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import javax.swing.JFrame;

import fr.lri.swingstates.canvas.CExtensionalTag;
import fr.lri.swingstates.canvas.CRectangle;
import fr.lri.swingstates.canvas.CShape;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.CTag;
import fr.lri.swingstates.canvas.CText;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.canvas.transitions.ClickOnShape;
import fr.lri.swingstates.canvas.transitions.EnterOnShape;
import fr.lri.swingstates.canvas.transitions.LeaveOnShape;
import fr.lri.swingstates.canvas.transitions.PressOnShape;
import fr.lri.swingstates.canvas.transitions.ReleaseOnShape;
import fr.lri.swingstates.debug.StateMachineVisualization;
import fr.lri.swingstates.sm.BasicInputStateMachine;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Click;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;
import fr.lri.swingstates.sm.transitions.TimeOut;

/**
 * @author Nicolas Roussel (roussel@lri.fr)
 *
 */
@SuppressWarnings({"unused", "static-access"})
public class SimpleButton {

	private CText label ;
	private CRectangle rect;
	private CStateMachine cs;

	//temps du demi-click en ms
	private static int demiClick;

	private static int modifier;
	private static int button;

	private static CTag simpleB;
	private static Canvas canvas;

	SimpleButton(Canvas canvas, String text, int b, int m, int demiClick) {
		rect = canvas.newRectangle(0, 0, 50, 50);
		label = canvas.newText(0, 0, text, new Font("verdana", Font.PLAIN, 12)) ;
		this.modifier = m;
		this.button = b;
		this.demiClick = demiClick;
		this.canvas = canvas;
		this.simpleB = new CExtensionalTag(canvas) {};
		
		rect.setFillPaint(Color.white);
		rect.addTag(simpleB);
		rect.addChild(label);
	}

	public CStateMachine getStateM(){

		final CExtensionalTag selected = new CExtensionalTag(canvas) {
			public void removed(CShape s) { 
				s.setFillPaint(Color.white);
			}
			public void added(CShape s){
				s.setFillPaint(Color.yellow);
			}

		};
		final CExtensionalTag demiClic = new CExtensionalTag(canvas) {
			public void removed(CShape s) { 
				s.setFillPaint(Color.white);
			}
			public void added(CShape s){
				s.setFillPaint(Color.blue);
			}
		};
		
		final CExtensionalTag clic = new CExtensionalTag(canvas) {
			public void removed(CShape s) { 
				s.setFillPaint(Color.white);
			}
			public void added(CShape s){
				s.setFillPaint(Color.magenta);
			}
		};

		final CExtensionalTag dblClick = new CExtensionalTag(canvas) {
			public void removed(CShape s) { 
				s.setFillPaint(Color.white);
			}
			public void added(CShape s){
				s.setFillPaint(Color.black);
			}
		};

		final CExtensionalTag failure = new CExtensionalTag(canvas) {
			public void removed(CShape s) { 
				s.setFillPaint(Color.white);
			}
			public void added(CShape s){
				s.setFillPaint(Color.red);
			}
		};

		return this.cs = new CStateMachine() {
			Color initColor;
			CShape s ;
			public State start = new State() {
				//Question 3
				//Question 4:(Sert aussi au demi-clic) Demi-clic: un demi-clic sur l'arrière-plan change sa couleur en bleu.
				Transition pressOnShape = new PressOnShape(button,">> s2"){
					public void action(){
						s = getShape();
						s.addTag(selected);
						s.setStroke(new BasicStroke(1));
						System.out.println("Etat start: Press on shape: la preuve il est devenu jaune.");
						armTimer(demiClick, false);
					}
				};
				//Question 3:
				Transition enterBox = new EnterOnShape(">> start"){
					public void action(){
						getShape().setStroke(new BasicStroke(1));
						getShape().setOutlined(true).setStroke(new BasicStroke(2));
						System.out.println("Etat start: in");
					}
				};
				//Question 3:
				Transition leaveBox = new LeaveOnShape(">> start"){
					public void action(){
						getShape().setStroke(new BasicStroke(1));
						System.out.println("Etat start: out");
					}
				};

				/*
				Transition press = new Press(">> s4"){
					public void action(){
						canvas.getFirstHavingTag(simpleB).addTag(demiClic);
						armTimer(demiClick, false);
					}
				};*/			
				//Question 4:Clic + demi-clic: si je fais un clic sur l'arrière-plan, le rectantgle rect devient violet (un clic = tag "clic").
				Transition clickBg = new Click(button,">> releaseBg"){
					public void action(){
						canvas.getFirstHavingTag(simpleB).addTag(clic);
						System.out.println("Etat start: Clic background!");
					}
				};
				//Question 4:Double-clic: après un click sur la "shape", sa couleur devient violet (un clic = tag "clic").
				Transition clickOnShape = new ClickOnShape(button,">> DblTheTrouble"){
					public void action(){
						s = getShape();
						s.addTag(clic);
						System.out.println("Etat start: ClickOnShape: vous avez 1000 ms pour faire un autre clic sur le bouton si vous le faîtes après les 1000ms il devient rouge. ;)");
						armTimer("dblClick",1000, false);
					}
				};
			};
			public State DblTheTrouble = new State(){
				//Question 4:Double-clic: vous avez 1000 ms pour faire un autre click si vous le faîtes après les 1000ms il devient rouge. 
				Transition dbl = new ClickOnShape(button,">> inter"){
					public void action(){
						s.removeTag(clic);
						s.addTag(dblClick);
						System.out.println("Etat dblTheTrouble: Double-click has succeeded!\nVous êtes bloqués (dans l'état inter).\nAppuyez sur le MODIFIER m et cliquez sur le BUTTON B passés en paramètre dans la classe SimpleButton pour vous débloquer.");
						disarmTimer("dblClick");
					}
				};

				//Question 4:Double-clic: timer déclenché car aucun clic n'a été effectué (la shape devient rouge).
				Transition timer = new TimeOut(">> inter"){
					public void action(){
						System.out.println("Etat dblTheTrouble: You failed the double-click!\n"
								+ "Vous êtes bloqués (dans l'état inter).\n"
								+ "Appuyez sur le MODIFIER m et cliquez sur le BUTTON B passés en paramètre dans la classe SimpleButton pour vous débloquer.");
						s.removeTag(clic);
						s.addTag(failure);
					}
				};
			};

			public State releaseBg = new State(){
				//Question 4:Clic + Demi-clic: si je presse sur l'arrière-plan avec n'importe quel bouton de la souris pendant demiClick ms alors toutes les shapes deviennent bleu.
				Transition press = new Press(button,">> lockOnBg"){
					public void action(){
						armTimer(demiClick, false);
					}
				};
				//Question 4:Clic + Demi-clic: si je presse sur le bouton alors retour au début (state start).
				Transition pressS = new PressOnShape(button,">> inter"){
					public void action(){
						s = getShape();
						s.removeTag(clic);
						s.addTag(failure);
						System.out.println("Etat releaseBg: Clic+Demi-clic: echec, vous avez pressé une shape après le clic au lieu de faire le demi-clic sur l'arrière-plan."
								+ "\nVous êtes bloqués (dans l'état inter).\n"
								+ "Appuyez sur le MODIFIER m et cliquez sur le BUTTON B passés en paramètre dans la classe SimpleButton pour vous débloquer.");
					}
				};

			};

			public State lockOnBg = new State(){
				//Question 4:Clic + Demi-clic: demi-clic réussi, le rectangle rect devient bleu. MODIFIER + BUT
				Transition timer = new TimeOut(">> inter"){
					public void action(){
						canvas.getFirstHavingTag(simpleB).removeTag(clic);
						canvas.getFirstHavingTag(simpleB).addTag(demiClic);
						System.out.println("Etat lockOnBG: Click + demi-clic réussi!\n"
								+ "Vous êtes bloqués (dans l'état inter).\n"
								+ "Appuyez sur le MODIFIER m et cliquez sur le BUTTON B passés en paramètre dans la classe SimpleButton pour vous débloquer.");
					}
				};
				//Question 4:Clic + Demi-clic: si vous relachez avant 500ms => Vous etes bloqués dans l'état inter et vous devez faire les commandes m + b (passées en param dans le constructeur de SimpleButton) pour vous débloquer.
				Transition release = new Release(button,">> inter"){
					public void action(){
						canvas.getFirstHavingTag(simpleB).removeTag(clic);
						canvas.getFirstHavingTag(simpleB).addTag(failure);
						System.out.println("Etat lockOnBg: Vous avez relâché le bouton avant les "+demiClick+" ms (demi-click raté).\n"
								+ "Vous êtes bloqués (dans l'état inter).\n"
								+ "Appuyez sur le MODIFIER m et cliquez sur le BUTTON B passés en paramètre dans la classe SimpleButton pour vous débloquer.");
					}
				};
			};

			//État intermédiaire pour les fonctionnalités (appuyez sur le MODIFIER m et cliquez sur le BUTTON B passés en paramètre dans la classe SimpleButton) pour retourner à l'état start).
			public State inter = new State (){
				Transition inter = new Click(button, modifier,">> start"){
					public void action(){
						System.out.println("Etat inter: Vous êtes débloqués.");
						if(s != null){
							if(s.hasTag(failure))
								s.removeTag(failure);
							if(s.hasTag(selected))
								s.removeTag(selected);
							if(s.hasTag(dblClick))
								s.removeTag(dblClick);
							if(s.hasTag(demiClic))
								s.removeTag(demiClic);
							s = null;
						}else{
							if(canvas.getFirstHavingTag(simpleB).hasTag(failure))
								canvas.getFirstHavingTag(simpleB).removeTag(failure);
							if(canvas.getFirstHavingTag(simpleB).hasTag(demiClic))
								canvas.getFirstHavingTag(simpleB).removeTag(demiClic);
						}
					}
				};
			};

			
			public State s2 = new State(){
				//Question 3
				Transition releaseOnShape = new ReleaseOnShape(button,">> start"){//s4
					public void action(){
						getShape().removeTag(selected);
						getShape().setOutlined(true).setStroke(new BasicStroke(2));
						System.out.println("Etat s2: Release on shape.");
					}
				};
				//Question 3
				Transition enterBox = new EnterOnShape(){
					public void action(){
						getShape().setStroke(new BasicStroke(1));
						getShape().addTag(selected);
					}
				};
				//Question 3
				Transition leaveBox = new LeaveOnShape(">> s3"){
					public void action(){
						getShape().removeTag(selected);
					}
				};
				//Question 3
				Transition release = new Release(button,">> start"){
					public void action(){
						s.removeTag(selected);
					}
				};

				//Question 4: Demi-clic: réussi.
				Transition timer = new TimeOut(){
					public void action(){
						s.removeTag(selected);
						s.addTag(demiClic);
						System.out.println("Etat s2: demi-clic réussi.");
						s.removeTag(demiClic);
						s.addTag(selected);
					}
				};
			};
			public State s4 = new State(){
				//Demi-click: réussi => état inter
				Transition timer =  new TimeOut(">> inter"){
					public void action(){
						System.out.println("Etat s4: Demi-click réussi!\n"
								+ "Vous êtes bloqués (dans l'état inter).\n"
								+ "Appuyez sur le MODIFIER m et cliquez sur le BUTTON B passés en paramètre dans la classe SimpleButton pour vous débloquer.");
						canvas.getFirstHavingTag(simpleB).removeTag(demiClic);
					}
				};
				//Demi-click: échoué => état inter
				Transition release = new Release(button,">> inter"){
					public void action(){
						System.out.println("Etat s4: Demi-click raté!\n"
								+ "Vous êtes bloqués (dans l'état inter).\n"
								+ "Appuyez sur le MODIFIER m et cliquez sur le BUTTON B passés en paramètre dans la classe SimpleButton pour vous débloquer.");
						canvas.getFirstHavingTag(simpleB).removeTag(demiClic);
						canvas.getFirstHavingTag(simpleB).addTag(failure);
					}
				};
			};

			//Question 3
			public State s3 = new State(){					
				Transition enterBox = new EnterOnShape(">> s2"){
					public void action(){
						getShape().addTag(selected);
						getShape().setStroke(new BasicStroke(1));
					}
				};

				Transition release = new Release(button,">> start"){
					public void action(){
					}
				};

			};

		};
	}

	public void action() {
		System.out.println("ACTION!") ;
	}

	public CShape getShape() {
		return rect ;
	}

	static public void main(String[] args) {
		JFrame frame = new JFrame() ;
		Canvas canvas = new Canvas(400,400) ;

		frame.getContentPane().add(canvas) ;
		frame.pack() ;
		frame.setVisible(true) ;
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) ;

		SimpleButton simple = new SimpleButton(canvas, "simple", BasicInputStateMachine.BUTTON1,BasicInputStateMachine.CONTROL_SHIFT, 500) ;
		//		SimpleButton simple2 = new SimpleButton(canvas, "simple2");

		simple.getShape().translateBy(50,200) ;
		//		simple2.getShape().translateBy(300, 50);



		JFrame jsm = new JFrame();
		//		JFrame jsm2 = new JFrame();

		StateMachineVisualization smv = new StateMachineVisualization(simple.getStateM());
		//		StateMachineVisualization smv2 = new StateMachineVisualization(sm3);
		jsm.add(smv);
		//		jsm2.add(smv2);
		jsm.getContentPane().add(smv);
		//		jsm2.getContentPane().add(smv2);
		jsm.pack() ;
		//		jsm2.pack();
		jsm.setVisible(false) ;
		//		jsm2.setVisible(true);
		simple.getStateM().attachTo(simple.getShape());
	}

}
