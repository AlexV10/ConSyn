import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import java.sql.*;

import javax.swing.*;

import java.security.*;

public class Main extends JFrame {

	private JLabel label;
	private JTextField textField;
	private JButton load;
	private JButton getSynonims;
	private JButton getMarksOfPatents;
	private JSeparator separator;

//	public Main() {
//		super("Поиск контекстных синонимов"); // Заголовок окна
//		setBounds(200, 200, 400, 200); // Если не выставить размер и положение -
//										// то окно будет мелкое и незаметное
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // это нужно для того
//														// чтобы при закрытии
//														// окна закрывалась и
//														// программа, иначе она
//														// останется висеть в
//														// процессах
//
//		load = new JButton("Загрузить патент");
//		getSynonims = new JButton("Получить контекстные синонимы");
//		getMarksOfPatents = new JButton("Сравнить с другими патентами");
//		separator = new JSeparator();
//		textField = new JTextField(20);
//		label = new JLabel("Введите id интересующего патента");
//
//		JPanel p = new JPanel();
//		p.setLayout(new FlowLayout());
//		p.add(load);
//		p.add(separator);
//		p.add(label);
//		p.add(textField);
//		p.add(getSynonims);
//		p.add(getMarksOfPatents);
//
//		add(p);
//
//	}

	public static ArrayList<ArrayList<Integer>> getClasters(
			ArrayList<ArrayList<Double>> arr, int elementsInClaster) {
		ArrayList<ArrayList<Integer>> res = new ArrayList<ArrayList<Integer>>();

		int SIZE = arr.size();
		// Построение матрицы достижимости
		ArrayList<ArrayList<Boolean>> arrD = new ArrayList<ArrayList<Boolean>>();

		for (int i = 0; i < SIZE; ++i) {
			ArrayList<Boolean> tmp = new ArrayList<Boolean>();
			for (int j = 0; j < SIZE; ++j) {
				if (arr.get(i).get(j) > 0.001)
					tmp.add(true);
				else
					tmp.add(false);
			}
			arrD.add(tmp);
		}
		// Построение матрицы переходов

		for (int i = 0; i < SIZE; ++i) {
			// ArrayList<Boolean> tmp = arrD.get(i);
			for (int j = 0; j < SIZE; ++j) {
				if (arrD.get(i).get(j) == true) {

					for (int k = 0; k < SIZE; ++k) {
						arrD.get(i).set(k,
								(arrD.get(j).get(k) || arrD.get(i).get(k)));
						arrD.get(k).set(i,
								(arrD.get(j).get(k) || arrD.get(i).get(k)));
					}
				}

			}
		}

		// Составляем список независимых графов
		for (int i = 0; i < SIZE; ++i) {
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			for (int j = 0; j < SIZE; ++j) {
				if (arrD.get(i).get(j) == true)
					tmp.add(j);
			}
			boolean flag = true;

			for (int k = 0; k < res.size(); ++k) {
				flag = true;
				if (res.get(k).get(0) == tmp.get(0)) // т.к. граф не
														// ориентирован - то
														// прокнет;)
				{
					flag = false;
					break;
				}
			}

			if (flag == true)
				res.add(tmp);

		}

		// проверка количества вершин в каждом независимом графе
		for (int i = 0; i < res.size(); ++i) {
			// если количество вершин превышает порог, разбиваем неугодный граф
			// на два и уходим в рекурсию. ми-ми-ми^^
			if (res.get(i).size() > elementsInClaster) {
				ArrayList<ArrayList<Double>> arr2 = new ArrayList<ArrayList<Double>>();

				// получаем инфу о весах дуг неугодного графа
				for (int j = 0; j < res.get(i).size(); ++j)
					arr2.add(arr.get(res.get(i).get(j)));

				Double min = 1.0;
				// / Ищем самую маленькую, но не нулевую связь и уничтожаем ее
				for (int k = 0; k < arr2.size(); ++k) {
					for (int l = 0; l < SIZE; ++l) {
						if (arr2.get(k).get(l) <= min
								&& arr2.get(k).get(l) > 0.00001)
							min = arr2.get(k).get(l);
					}
				}

				for (int k = 0; k < res.get(i).size(); ++k) {
					for (int l = 0; l < SIZE; ++l) {

						int qwer = res.get(i).get(k);
						if (res.get(i).get(k) == l)
							continue;
						Double qwe = arr.get(res.get(i).get(k)).get(l); // так
																		// надежней,
																		// чем
																		// напрямую
																		// вставлять
																		// в if
						if (qwe - min < 0.00001)
							arr.get(res.get(i).get(k)).set(l, 0.0);
					}
				}

				return getClasters(arr, elementsInClaster);
			}
		}

		return res;

	}

	public static ArrayList<GramBasics> getGramBasics(String text)
			throws MalformedURLException, IOException,
			ParserConfigurationException, SAXException, SQLException {
		// выделение предложений из ихсодного текста
		String[] lines = text.split("\n");

		ArrayList<String> sens = new ArrayList<String>();
		String tmp = "";
		for (int i = 0; i < lines.length; i++) {

			if (!lines[i].isEmpty())
				tmp += lines[i].toString() + "\n";
			else {
				sens.add(tmp);
				tmp = "";
			}
		}
		sens.add(tmp);

		// Выделение грамматических основ

		ArrayList<GramBasics> gramBasics = new ArrayList<GramBasics>();
		for (int l = 0; l < sens.size(); ++l) {
			String[] words = sens.get(l).split("\n");

			for (int i = 0; i < words.length; ++i) {
				String[] param = words[i].toString().split("\t");
				if (param[3].equals("V") || param[3].equals("VBP") || param[3].equals("VBD") || param[3].equals("VBG") || param[3].equals("VBN") || param[3].equals("VBZ") || param[3].equals("VB")) // если глагол, то ищем зависимые
											// слова.
				{
					boolean hasN = false;
					ArrayList<String> nouns = new ArrayList<String>();
					ArrayList<String> nouns2 = new ArrayList<String>();
					for (int j = 0; j < words.length; ++j) {
						String[] param2 = words[j].toString().split("\t");
						if ((param2[3].equals("N") && param2[6].equals(param[0])) || (param2[3].equals("NN") && param2[6].equals(param[0]))) {
							hasN = true;
							if (!param2[2].equals("<unknown>"))
								nouns.add(param2[2]);
							else
								nouns.add(param2[1]);

							// вот здесь надо искать!!

							for (int k = 0; k < words.length; ++k) {
								String[] param3 = words[k].toString().split(
										"\t");
								// зависимые слова не находятся....чтож...берем
								// просто остальные существительные
								if ((param3[3].equals("N")	&& !param3[2].equals(param2[2])) || (param3[3].equals("NN")	&& !param3[2].equals(param2[2]))) {
									if (!param3[2].equals("<unknown>"))
										nouns2.add(param3[2]);
									else
										nouns2.add(param3[1]);
								}

							}

						}
					}
					// Сохраняем глагол и сущесвтителньые иже с ним
					if (hasN) {
						GramBasics temp = new GramBasics();
						String tmpRes = "";
						if (!param[2].equals("<unknown>"))
							temp.verb = param[2].toString();
						else
							temp.verb = param[1].toString();

						temp.nouns = nouns;
						temp.nouns2 = nouns2;
						temp.synonims = Synonims.getSynonims(temp.verb);

						gramBasics.add(temp);
					}
				}
			}
		}
		return gramBasics;
	}

	public static Doc getDocument(String filename, String _class, String name)
			throws MalformedURLException, IOException,
			ParserConfigurationException, SAXException, SQLException {
		String text = FileRW.fileRead(filename);
		ArrayList<GramBasics> gramBasics = getGramBasics(text);

		// УДАЛЕНИЕ ПОВТОРОВ ЛОМАЕТ ПОРЯДОК ПРЕДЛОЖЕНИЙ В ТЕКСТЕ.
		// не фатально, но с толку сбивает :(:(:(:(
		gramBasics = GramBasics.removeRepetitions(gramBasics);

		// for (int i=0; i<gramBasics.size(); ++i)
		// {
		// System.out.print(GramBasics.gramBasicToString(gramBasics.get(i))+"\n\n\n");
		// }

		//
		// /// код кластеризации
		int SIZE = gramBasics.size();

		ArrayList<ArrayList<Double>> arr = new ArrayList<ArrayList<Double>>();

		// инициализация 0
		for (int i = 0; i < SIZE; ++i) {
			ArrayList<Double> tmp = new ArrayList<Double>();
			for (int j = 0; j < SIZE; ++j) {
				tmp.add(Synonims.getMark(gramBasics.get(i), gramBasics.get(j)));
			}
			arr.add(tmp);
		}

		// for(int i=0; i < gramBasics.get(0).synonims.size(); ++i)
		// System.out.println(gramBasics.get(0).synonims.get(i));
		//
		// System.out.print("go");
		ArrayList<ArrayList<Integer>> res = getClasters(arr, 5);

		// возможно следует написат ьфункцию возвращающую оценку схожести,
		// между элементами кластеров.... некоторые элементы ну очень неявные

		ArrayList<Claster> clasters = new ArrayList<Claster>();

		for (int i = 0; i < res.size(); ++i) {
			System.out.println("Кластер " + i + ":\n");
			ArrayList<GramBasics> tmp = new ArrayList<GramBasics>();
			for (int j = 0; j < res.get(i).size(); ++j) {

				tmp.add(gramBasics.get(res.get(i).get(j)));
				// System.out.println(gramBasics.get(res.get(i).get(j)).verb+"; ");
				System.out.println(GramBasics.gramBasicToString(gramBasics
						.get(res.get(i).get(j))));
			}
			clasters.add(new Claster(tmp));
			System.out.println("\n");
		}
		return new Doc(clasters, _class, name);

		// вывод на экран с оценками. повторы есть..лень исправлять сейчас
		// for(int i=0; i<res.size(); ++i)
		// {
		// System.out.println("Кластер "+i+":\n");
		//
		// for (int j=0; j<res.get(i).size(); ++j)
		// {
		// for(int k=0; k<res.get(i).size(); ++k)
		// {
		//
		// System.out.println(gramBasics.get(res.get(i).get(j)).verb + " - " +
		// gramBasics.get(res.get(i).get(k)).verb + ": " +
		// arr.get(res.get(i).get(j)).get(res.get(i).get(k)) + "\n");
		// }
		//
		//
		// }
		// System.out.println("\n");
		// }

	}

	public static double getMarkOfSimilarityDoc(Doc test0, Doc test1) {
		Doc docA, docB;
		// перекидываются лишь ссылки, но думаю будет достаточно. иначе
		// колдовать с конструктором
		if (test0.words <= test1.words) {
			docA = test0;
			docB = test1;
		} else {
			docA = test1;
			docB = test0;
		}

		double maxMark = docA.words; // берем по меньшему документу

		// сравниваем главные существительные. 0 0.6 0.8 1

		double markNouns = 0;

		// АААААААААААААААААААААААААААААААААААААААААААД

		for (int i = 0; i < docA.clasters.size(); ++i) // стучимся к кластеру
		{
			for (int j = 0; j < docA.clasters.get(i).gramBasics.size(); ++j) // стучимся
																				// к
																				// грам.основам
			{
				for (int k = 0; k < docA.clasters.get(i).gramBasics.get(j).nouns
						.size(); ++k) // к осн. сущ.
				{
					String nounA = docA.clasters.get(i).gramBasics.get(j).nouns
							.get(k); // достучались!^^
					ArrayList<String> nounVerbA = new ArrayList<String>();
					ArrayList<String> synA = new ArrayList<String>();
					for (int l = 0; l < docA.clasters.get(i).gramBasics.size(); ++l) {
						nounVerbA
								.add(docA.clasters.get(i).gramBasics.get(l).verb);
						synA.addAll(docA.clasters.get(i).gramBasics.get(l).synonims);
					}

					String tmpRes = nounA + " 0";
					double tmpMark = 0.0; // ищем максималньую оценку для
											// данного слова

					// перебираем второй документ...выход безусловным переходом.
					// йа быдло.
					endLoop: for (int m = 0; m < docB.clasters.size(); ++m) // стучимся
																			// к
																			// кластеру
					{
						for (int n = 0; n < docB.clasters.get(m).gramBasics
								.size(); ++n) // стучимся к грам.основам
						{
							for (int o = 0; o < docB.clasters.get(m).gramBasics
									.get(n).nouns.size(); ++o) // к осн. сущ.
							{
								String nounB = docB.clasters.get(m).gramBasics
										.get(n).nouns.get(o); // достучались!^^

								ArrayList<String> nounVerbB = new ArrayList<String>();
								for (int p = 0; p < docB.clasters.get(m).gramBasics
										.size(); ++p) {
									nounVerbB
											.add(docB.clasters.get(m).gramBasics
													.get(p).verb);
								}

								if (nounA.equals(nounB)) {
									if (tmpMark < 0.5) // совпадение только
														// существительного
									{
										tmpMark = 0.6;
										tmpRes = nounA + " " + nounB + " 0.6";
									}

									for (int p = 0; p < nounVerbA.size(); ++p) // проверка
																				// на
																				// совпадение
																				// сущ+гл.
									{
										for (int l = 0; l < nounVerbB.size(); ++l) {
											if (nounVerbA.get(p).equals(
													nounVerbB.get(l))) {
												tmpMark = 1.0;
												tmpRes = nounA + " " + nounB
														+ " "
														+ nounVerbA.get(p)
														+ " "
														+ nounVerbB.get(l)
														+ " 1.0";
												break endLoop; // максималньая
																// оценка. НАХУЙ
																// ИЗ ЭТОГО АДА.
																// БЕЗУСЛОВНО
																// НАХУЙ!
											}
										}
									}

									if (tmpMark < 0.7) // проверка на совпадение
														// через синоним...
									{
										endLoopSyn: for (int p = 0; p < nounVerbB
												.size(); ++p) {
											for (int l = 0; l < synA.size(); ++l) {
												if (nounVerbB.get(p).equals(
														synA.get(l))) {
													tmpMark = 0.8;
													tmpRes = nounA + " "
															+ nounB + " "
															+ synA.get(l) + " "
															+ nounVerbB
															+ " 0.8";
													break endLoopSyn;
												}
											}
										}
									}

								}
							}
						}
					}

					System.out.println(tmpRes + "\n");
					// вышли из проверки второго документа
					markNouns += tmpMark;

				}
			}

		}

		// конец цикла!
		System.out.println(markNouns);

		// сравнение побочных существительных 0.5 0.7 1 ...копипаста..мб есть
		// ошибки, но пока не увидел

		double markNouns2 = 0;

		// АААААААААААААААААААААААААААААААААААААААААААД2

		for (int i = 0; i < docA.clasters.size(); ++i) // стучимся к кластеру
		{
			for (int j = 0; j < docA.clasters.get(i).gramBasics.size(); ++j) // стучимся
																				// к
																				// грам.основам
			{
				for (int k = 0; k < docA.clasters.get(i).gramBasics.get(j).nouns2
						.size(); ++k) // к осн. сущ.
				{
					String nounA = docA.clasters.get(i).gramBasics.get(j).nouns2
							.get(k); // достучались!^^
					ArrayList<String> nounVerbA = new ArrayList<String>();
					ArrayList<String> synA = new ArrayList<String>();
					for (int l = 0; l < docA.clasters.get(i).gramBasics.size(); ++l) {
						nounVerbA
								.add(docA.clasters.get(i).gramBasics.get(l).verb);
						synA.addAll(docA.clasters.get(i).gramBasics.get(l).synonims);
					}

					String tmpRes = nounA + " 0";
					double tmpMark = 0.0; // ищем максималньую оценку для
											// данного слова

					// перебираем второй документ..по вспомогательным
					// сущесвтительным
					endLoop2: for (int m = 0; m < docB.clasters.size(); ++m) // стучимся
																				// к
																				// кластеру
					{
						for (int n = 0; n < docB.clasters.get(m).gramBasics
								.size(); ++n) // стучимся к грам.основам
						{
							for (int o = 0; o < docB.clasters.get(m).gramBasics
									.get(n).nouns2.size(); ++o) // к осн. сущ.
							{
								String nounB = docB.clasters.get(m).gramBasics
										.get(n).nouns2.get(o); // достучались!^^

								ArrayList<String> nounVerbB = new ArrayList<String>();
								for (int p = 0; p < docB.clasters.get(m).gramBasics
										.size(); ++p) {
									nounVerbB
											.add(docB.clasters.get(m).gramBasics
													.get(p).verb);
								}

								if (nounA.equals(nounB)) {
									if (tmpMark < 0.6) // совпадение только
														// существительного
									{
										tmpMark = 0.5;
										tmpRes = nounA + " " + nounB + " 0.5";
									}

									for (int p = 0; p < nounVerbA.size(); ++p) // проверка
																				// на
																				// совпадение
																				// сущ+гл.
									{
										for (int l = 0; l < nounVerbB.size(); ++l) {
											if (nounVerbA.get(p).equals(
													nounVerbB.get(l))) {
												tmpMark = 1.0;
												tmpRes = nounA + " " + nounB
														+ " "
														+ nounVerbA.get(p)
														+ " "
														+ nounVerbB.get(l)
														+ " 1.0";
												break endLoop2; // максималньая
																// оценка. НАХУЙ
																// ИЗ ЭТОГО АДА.
																// БЕЗУСЛОВНО
																// НАХУЙ!
											}
										}
									}

									if (tmpMark < 0.6) // проверка на совпадение
														// через синоним...
									{
										endLoopSyn2: for (int p = 0; p < nounVerbB
												.size(); ++p) {
											for (int l = 0; l < synA.size(); ++l) {
												if (nounVerbB.get(p).equals(
														synA.get(l))) {
													tmpMark = 0.7;
													tmpRes = nounA + " "
															+ nounB + " "
															+ synA.get(l) + " "
															+ nounVerbB
															+ " 0.7";
													break endLoopSyn2;
												}
											}
										}
									}

								}
							}
						}
					}

					// проверка второго документа по основным существительным
					if (tmpMark < 0.8) {
						// перебираем второй документ..по вспомогательным
						// сущесвтительным
						endLoop3: for (int m = 0; m < docB.clasters.size(); ++m) // стучимся
																					// к
																					// кластеру
						{
							for (int n = 0; n < docB.clasters.get(m).gramBasics
									.size(); ++n) // стучимся к грам.основам
							{
								for (int o = 0; o < docB.clasters.get(m).gramBasics
										.get(n).nouns.size(); ++o) // к осн.
																	// сущ.
								{
									String nounB = docB.clasters.get(m).gramBasics
											.get(n).nouns.get(o); // достучались!^^

									ArrayList<String> nounVerbB = new ArrayList<String>();
									for (int p = 0; p < docB.clasters.get(m).gramBasics
											.size(); ++p) {
										nounVerbB
												.add(docB.clasters.get(m).gramBasics
														.get(p).verb);
									}

									if (nounA.equals(nounB)) {
										if (tmpMark < 0.6) // совпадение только
															// существительного
										{
											tmpMark = 0.5;
											tmpRes = nounA + " " + nounB
													+ " 0.5";
										}

										for (int p = 0; p < nounVerbA.size(); ++p) // проверка
																					// на
																					// совпадение
																					// сущ+гл.
										{
											for (int l = 0; l < nounVerbB
													.size(); ++l) {
												if (nounVerbA.get(p).equals(
														nounVerbB.get(l))) {
													tmpMark = 1.0;
													tmpRes = nounA + " "
															+ nounB + " "
															+ nounVerbA.get(p)
															+ " "
															+ nounVerbB.get(l)
															+ " 1.0";
													break endLoop3; // максималньая
																	// оценка.
																	// НАХУЙ ИЗ
																	// ЭТОГО
																	// АДА.
																	// БЕЗУСЛОВНО
																	// НАХУЙ!
												}
											}
										}

										if (tmpMark < 0.6) // проверка на
															// совпадение через
															// синоним...
										{
											endLoopSyn3: for (int p = 0; p < nounVerbB
													.size(); ++p) {
												for (int l = 0; l < synA.size(); ++l) {
													if (nounVerbB
															.get(p)
															.equals(synA.get(l))) {
														tmpMark = 0.7;
														tmpRes = nounA + " "
																+ nounB + " "
																+ synA.get(l)
																+ " "
																+ nounVerbB
																+ " 0.7";
														break endLoopSyn3;
													}
												}
											}
										}

									}
								}
							}
						}
					}

					System.out.println(tmpRes + "\n");
					// вышли из проверки второго документа
					markNouns2 += tmpMark;

				}
			}

		}
		System.out.println(markNouns2);
		System.out.println("Максимальная оценка:" + maxMark);
		System.out.println("Оценка сходства:" + (markNouns + markNouns2)
				/ maxMark);
		return (markNouns + markNouns2) / maxMark;
	}

	public static double[] getMarkOfClasters(Claster a, Claster b) {
		double[] resClaster = { -1.0, // общий глагол -1 или 1
				-1.0, // есть общее сущ -1 или 1
				-1.0, // оценка сходства по глаголам 0..1
				-1.0, // оценка сходства по существительным 0.1
				-1.0 }; // средняя оценка 0..1
		Claster c0, c1;
		c0 = new Claster(a);
		c1 = new Claster(b);

		// Ищем наличие общего глагола

		clastExit0: for (int i0 = 0; i0 < c0.countOfGB; ++i0) // берем грам.
																// основы
		{
			String verb0 = c0.gramBasics.get(i0).verb; // глагол1...

			for (int i1 = 0; i1 < c1.countOfGB; ++i1) // копаем во второй
														// кластер
			{
				for (int i2 = 0; i2 < c1.gramBasics.get(i1).synonims.size(); ++i2) // копаем
																					// синонимы
																					// и
																					// сравниваем
				{
					if (verb0.equals(c1.gramBasics.get(i1).synonims.get(i2))) {
						resClaster[0] = 1.0;
						break clastExit0;
					}
				}
			}
		}

		if (resClaster[0] < 0.9)
			return resClaster; // финиш если не нашли

		//System.out.println(resClaster[0]);

		// сравнение главных существительных...
		clastExit1: for (int i0 = 0; i0 < c0.countOfGB; ++i0) {
			for (int i1 = 0; i1 < c0.gramBasics.get(i0).nouns.size(); ++i1) {
				String n0 = c0.gramBasics.get(i0).nouns.get(i1);
				// копаем во второй кластер
				for (int i2 = 0; i2 < c1.countOfGB; ++i2) {
					for (int i3 = 0; i3 < c1.gramBasics.get(i2).nouns.size(); ++i3) {
						if (n0.equals(c1.gramBasics.get(i2).nouns.get(i3))) {
							resClaster[1] = 1.0;
							break clastExit1;
						}
					}

					for (int i4 = 0; i4 < c1.gramBasics.get(i2).nouns2.size(); ++i4) {
						if (n0.equals(c1.gramBasics.get(i2).nouns2.get(i4))) {
							resClaster[1] = 1.0;
							break clastExit1;
						}
					}
				}
			}
		}

		// сравнение побочных существительных...
		clastExit2: for (int i0 = 0; i0 < c0.countOfGB; ++i0) {
			for (int i1 = 0; i1 < c0.gramBasics.get(i0).nouns2.size(); ++i1) {
				String n0 = c0.gramBasics.get(i0).nouns2.get(i1);
				// копаем во второй кластер
				for (int i2 = 0; i2 < c1.countOfGB; ++i2) {
					for (int i3 = 0; i3 < c1.gramBasics.get(i2).nouns.size(); ++i3) {
						if (n0.equals(c1.gramBasics.get(i2).nouns.get(i3))) {
							resClaster[1] = 1.0;
							break clastExit2;
						}
					}

					for (int i4 = 0; i4 < c1.gramBasics.get(i2).nouns2.size(); ++i4) {
						if (n0.equals(c1.gramBasics.get(i2).nouns2.get(i4))) {
							resClaster[1] = 1.0;
							break clastExit2;
						}
					}
				}
			}
		}

		//
		if (resClaster[1] < 0.9)
			return resClaster;

		//System.out.println(resClaster[1]);

		// !!!! Оценка сходства по глаголам

		ArrayList<String> _syn0 = new ArrayList<String>();
		ArrayList<String> _syn1 = new ArrayList<String>();

		for (int i = 0; i < c0.countOfGB; ++i) {
			_syn0.addAll(c0.gramBasics.get(i).synonims);
		}

		// удаляем повторы

		ArrayList<String> syn0 = new ArrayList<String>(new HashSet<String>(
				_syn0));
		Collections.sort(syn0);

		for (int i = 0; i < c1.countOfGB; ++i) {
			_syn1.addAll(c1.gramBasics.get(i).synonims);
		}

		// удаляем повторы

		ArrayList<String> syn1 = new ArrayList<String>(new HashSet<String>(
				_syn1));
		Collections.sort(syn1);

		double synCountAll = syn0.size() + syn1.size();

		ArrayList<String> _synAll = new ArrayList<String>();
		_synAll.addAll(syn0);
		_synAll.addAll(syn1);

		ArrayList<String> synAll = new ArrayList<String>(new HashSet<String>(
				_synAll));
		Collections.sort(synAll);

		double ss = synAll.size();

		resClaster[2] = (synCountAll - ss) / ss;

		//System.out.println(resClaster[2]);

		// !!!! Сравнение по существительным

		ArrayList<String> _n0 = new ArrayList<String>();
		ArrayList<String> _n1 = new ArrayList<String>();

		for (int i = 0; i < c0.countOfGB; ++i) {
			_n0.addAll(c0.gramBasics.get(i).nouns);
			_n0.addAll(c0.gramBasics.get(i).nouns2);
		}
		// удаляем повторы
		ArrayList<String> n0 = new ArrayList<String>(new HashSet<String>(_n0));
		Collections.sort(n0);

		for (int i = 0; i < c1.countOfGB; ++i) {
			_n1.addAll(c1.gramBasics.get(i).nouns);
			_n1.addAll(c1.gramBasics.get(i).nouns2);
		}

		// удаляем повторы

		ArrayList<String> n1 = new ArrayList<String>(new HashSet<String>(_n1));
		Collections.sort(n1);

		double nCountAll = n0.size() + n1.size();

		ArrayList<String> _nAll = new ArrayList<String>();
		_nAll.addAll(n0);
		_nAll.addAll(n1);

		ArrayList<String> nAll = new ArrayList<String>(new HashSet<String>(
				_nAll));
		Collections.sort(nAll);

		double ssn = nAll.size();

		resClaster[3] = (nCountAll - ssn) / ssn;

		//System.out.println(resClaster[3]);

		resClaster[4] = (resClaster[2] + resClaster[3]) / 2.0;

		//System.out.println(resClaster[4]);

		return resClaster;
	}

	public static Claster getClasterFromBase(String idClaster)
			throws SQLException {

		ArrayList<GramBasics> sentenses = new ArrayList<GramBasics>();
		// / пишем в базу документы
		// стучимся к базе на предмет существования подобного глагола
		//System.out.println("-------- PostgreSQL "
				//+ "JDBC Connection Testing ------------");

		try {

			Class.forName("org.postgresql.Driver");

		} catch (ClassNotFoundException e) {

			//System.out.println("Where is your PostgreSQL JDBC Driver? "
				//	+ "Include in your library path!");
			e.printStackTrace();

		}

		//System.out.println("PostgreSQL JDBC Driver Registered!");

		Connection conn = null;

		try {

			conn = DriverManager.getConnection(
					"jdbc:postgresql://127.0.0.1:5432/testENG", "postgres",
					"1234");

		} catch (SQLException e) {

			//System.out.println("Connection Failed! Check output console");
			e.printStackTrace();

		}

		if (conn != null) {
			Statement st = conn.createStatement();
			ResultSet rs;
			// rs =
			// st.executeQuery("SELECT \"idSens\", nouns, nouns2, verb, synonims FROM \"Sens\" "
			// +
			// "WHERE \"idClaster\" = " +idClaster+ ";");
			rs = st.executeQuery("SELECT \"idSens\" FROM \"Sens\" "
					+ "WHERE \"idClaster\" = " + idClaster + ";");

			ArrayList<String> idSens = new ArrayList<String>();

			while (rs.next()) {
				idSens.add(rs.getString("idSens"));

			}

			//for (int i = 0; i < idSens.size(); ++i) {
			//	System.out.println(idSens.get(i));
			//}
			rs.close();

			for (int i = 0; i < idSens.size(); ++i) {
				GramBasics tmpGB = new GramBasics();
				rs = st.executeQuery("SELECT nouns, nouns2, verb, synonims FROM \"Sens\" "
						+ "WHERE \"idSens\" = " + idSens.get(i) + ";");
				while (rs.next()) {
					String _nouns = rs.getString("nouns");
					String __nouns[] = _nouns.split(";");

					for (int j = 0; j < __nouns.length; ++j) {
						tmpGB.nouns.add(__nouns[j]);
					}

					String _nouns2 = rs.getString("nouns2");
					String __nouns2[] = _nouns2.split(";");

					for (int j = 0; j < __nouns2.length; ++j) {
						tmpGB.nouns2.add(__nouns2[j]);
					}

					String _syn = rs.getString("synonims");
					String __syn[] = _syn.split(";");

					for (int j = 0; j < __syn.length; ++j) {
						tmpGB.synonims.add(__syn[j]);
					}

					tmpGB.verb = rs.getString("verb");

					sentenses.add(tmpGB);

				}

				rs.close();

			}

		}

		conn.close();

		return new Claster(sentenses);
	}
	
	public static void addDocToBase(Doc test0) throws SQLException
	{
		System.out.println("Начата запись в базу...");
		// / пишем в базу документы
				// стучимся к базе на предмет существования подобного глагола
				//System.out.println("-------- PostgreSQL "
				//		+ "JDBC Connection Testing ------------");

				try
				{

					Class.forName("org.postgresql.Driver");

				} catch (ClassNotFoundException e) {

					//System.out.println("Where is your PostgreSQL JDBC Driver? "
						//	+ "Include in your library path!");
					e.printStackTrace();

				}

				//System.out.println("PostgreSQL JDBC Driver Registered!");

				Connection connection = null;

				try {

					connection = DriverManager.getConnection(
							"jdbc:postgresql://127.0.0.1:5432/testENG", "postgres",
							"1234");

				} catch (SQLException e) {

					//System.out.println("Connection Failed! Check output console");
					e.printStackTrace();

				}

				if (connection != null) {
					//System.out.println("You made it, take control your database now!");
					Statement st = connection.createStatement();
					System.out.println("Добавляю документ...");
					String sql = "INSERT INTO \"Patent\"(class, name)  VALUES ('"
							+ test0._class + "', '" + test0.name + "');";

					// пишем патент
					st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
					ResultSet keyset = st.getGeneratedKeys();
					long idDocInBase = -1;
					if (keyset.next()) {
						// Retrieve the auto generated key(s).
						idDocInBase = keyset.getLong(1);
						//System.out.println("Айдишник дока=" + idDocInBase);
					}

					st.close();
					// пишем кластеры
					System.out.println("Добавляю кластеры...");
					ArrayList<Long> idClasterInBase = new ArrayList<Long>();
					for (int i = 0; i < test0.clasters.size(); ++i) {
						
						st = connection.createStatement();
						sql = "INSERT INTO \"Claster\"(\"idPatent\")  VALUES ("
								+ Long.toString(idDocInBase) + ");";

						st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
						keyset = st.getGeneratedKeys();

						if (keyset.next()) {
							// Retrieve the auto generated key(s).
							idClasterInBase.add(keyset.getLong(1));
							//System.out.println("Айдишник кластера="
								//	+ idClasterInBase.get(idClasterInBase.size() - 1));
						}
						st.close();
						// пишем предложения
						for (int j = 0; j < test0.clasters.get(i).gramBasics.size(); ++j) {
							String _nouns = "", _nouns2 = "", _verb = "", _synonims = "";
							for (int k = 0; k < test0.clasters.get(i).gramBasics.get(j).nouns
									.size(); ++k) {
								if (_nouns.length()
										+ test0.clasters.get(i).gramBasics.get(j).nouns
												.get(k).length() > 1020)
									break;
								_nouns += test0.clasters.get(i).gramBasics.get(j).nouns
										.get(k) + ";";
							}

							for (int k = 0; k < test0.clasters.get(i).gramBasics.get(j).nouns2
									.size(); ++k) {
								if (_nouns2.length()
										+ test0.clasters.get(i).gramBasics.get(j).nouns2
												.get(k).length() > 1020)
									break;
								_nouns2 += test0.clasters.get(i).gramBasics.get(j).nouns2
										.get(k) + ";";
							}

							for (int k = 0; k < test0.clasters.get(i).gramBasics.get(j).synonims
									.size(); ++k) {
								if (_synonims.length()
										+ test0.clasters.get(i).gramBasics.get(j).synonims
												.get(k).length() > 1020)
									break;
								_synonims += test0.clasters.get(i).gramBasics.get(j).synonims
										.get(k) + ";";
							}

							_verb = test0.clasters.get(i).gramBasics.get(j).verb;

							st = connection.createStatement();
							sql = "INSERT INTO \"Sens\"(nouns, nouns2, verb, synonims,\"idClaster\") "
									+ " VALUES ('"
									+ _nouns
									+ "','"
									+ _nouns2
									+ "','"
									+ _verb
									+ "','"
									+ _synonims
									+ "',"
									+ idClasterInBase.get(idClasterInBase.size() - 1)
									+ ");";

							// по идее предложения сэйвить не надо..простйо экзекьют
							st.executeUpdate(sql);

							st.close();
							keyset.close();
						}

					}
					st = connection.createStatement();
					ResultSet rs;
					System.out.println("Выставляю оценки...");
					rs = st.executeQuery("SELECT \"idClaster\"  FROM \"Claster\" WHERE \"idPatent\" = "
							+ Long.toString(idDocInBase) + ";");

					ArrayList<String> idClasters = new ArrayList<String>();
					while (rs.next()) {
						idClasters.add(rs.getString("idClaster"));
					}

					rs.close();
					st.close();

					// последний цикл диплома жи....заработай сразу ПЛИЗКИ Т_Т
					// добавляем глобал кластер
					for (int k = 0; k < idClasters.size(); ++k) {
						Claster globalClaster = getClasterFromBase(idClasters.get(k));
						System.out.println("Обрабатываю "+(k+1)+" кластер из "+idClasters.size());
						// нужна проверка на существование данного глобал кластера.
						// выбираем все кластеры
						st = connection.createStatement();
						rs = st.executeQuery("SELECT \"idClaster\"  FROM \"Claster\";");

						ArrayList<String> idClastersGB = new ArrayList<String>();
						while (rs.next()) {
							idClastersGB.add(rs.getString("idClaster"));
						}

						rs.close();
						st.close();

						boolean alreadyExist = false;
						for (int l = 0; l < idClastersGB.size(); ++l) 
						{
							if(idClastersGB.get(l).equals(idClasters.get(k)))
								continue;
							Claster tmpC = getClasterFromBase(idClastersGB.get(l));
							double[] mark = getMarkOfClasters(globalClaster, tmpC);
							if (mark[4] > 0.99) {
								alreadyExist = true;
								break;
							}
						}
						

						// вставляем...если такого глобал кластера нет
						if (alreadyExist == false) 
						{
							st = connection.createStatement();
							sql = "INSERT INTO \"GlobalClaster\"(\"idClaster\") VALUES ("
									+ idClasters.get(k) + ");";

							st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
							keyset = st.getGeneratedKeys();

							// получаем айдишник созданного глобал кластера
							long idGC = -1;
							if (keyset.next()) {

								idGC = keyset.getLong(1);
								//System.out.println("Айдишник глобал кластера=" + idGC);
							}

							keyset.close();
							st.close();

							// получили, теперь циклично перебираем их..и если есть
							// совпадение то делаем запись в табилцу марк
							for (int l = 0; l < idClastersGB.size(); ++l) {
								Claster tmpC = getClasterFromBase(idClastersGB.get(l));
								double[] mark = getMarkOfClasters(globalClaster, tmpC);
								// если совпадения есть - то вставляем в базу таблицу
								// марк
								if (mark[0] > 0 && mark[1] > 0) {
									st = connection.createStatement();
									sql = "INSERT INTO \"Mark\"(\"idClaster\", \"idGlobalClaster\", mark) "
											+ "VALUES ("
											+ idClastersGB.get(l)
											+ ","
											+ Long.toString(idGC)
											+ ","
											+ Double.toString(mark[4]) + ");";

									st.executeUpdate(sql);
									st.close();
								}

							}
						}

					}

					connection.close();
				}
	}

	public static Doc getDocFromBase(String idDoc) throws SQLException
	{
		String _name ="";
		String _class="";
		ArrayList<Claster> arrClaster = new ArrayList<Claster>();
		
		// стучимся к базе на предмет существования подобного глагола
		//System.out.println("-------- PostgreSQL "
				//+ "JDBC Connection Testing ------------");

		try
		{

			Class.forName("org.postgresql.Driver");

		} catch (ClassNotFoundException e) {

			//System.out.println("Where is your PostgreSQL JDBC Driver? "
				//	+ "Include in your library path!");
			e.printStackTrace();

		}

		//System.out.println("PostgreSQL JDBC Driver Registered!");

		Connection connection = null;

		try {

			connection = DriverManager.getConnection(
					"jdbc:postgresql://127.0.0.1:5432/testENG", "postgres",
					"1234");

		} catch (SQLException e) {

			//System.out.println("Connection Failed! Check output console");
			e.printStackTrace();

		}

		if (connection != null) {
			//System.out.println("You made it, take control your database now!");
			
			Statement st = connection.createStatement();
			ResultSet rs;
			rs = st.executeQuery("SELECT \"idClaster\"  FROM \"Claster\" WHERE \"idPatent\" = "
					+ idDoc + ";");

			ArrayList<String> idClasters = new ArrayList<String>();
			while (rs.next()) {
				idClasters.add(rs.getString("idClaster"));
			}

			rs.close();
			st.close();
			
			for(int i=0; i<idClasters.size(); ++i)
			{
				arrClaster.add(getClasterFromBase(idClasters.get(i)));
			}
			
			st = connection.createStatement();
			rs = st.executeQuery("SELECT class, name FROM \"Patent\" WHERE \"idPatent\"="+idDoc+";");

			
			while (rs.next()) {
				_class = rs.getString("class");
				_name = rs.getString("name");
			}

			rs.close();
			st.close();
			connection.close();
			
			
		}
		return new Doc(arrClaster, _class, _name);
		
	}
	
	public static ArrayList<String> getLecsemsFromClaster(Claster c)
	{
		ArrayList<String> res = new ArrayList<String>();
		
		for(int i=0; i<c.countOfGB; ++i)
		{
			res.addAll(c.gramBasics.get(i).nouns);
			res.addAll(c.gramBasics.get(i).nouns2);
		}
		
		return res;
	}
	
	public static ArrayList<String> getCS(String idDoc) throws SQLException
	{
	ArrayList<String> cs = new ArrayList<String>();
		
		try
		{

			Class.forName("org.postgresql.Driver");

		} catch (ClassNotFoundException e) {

			//System.out.println("Where is your PostgreSQL JDBC Driver? "
				//	+ "Include in your library path!");
			e.printStackTrace();

		}

		//System.out.println("PostgreSQL JDBC Driver Registered!");

		Connection connection = null;

		try {

			connection = DriverManager.getConnection(
					"jdbc:postgresql://127.0.0.1:5432/testENG", "postgres",
					"1234");

		} catch (SQLException e) {

			//System.out.println("Connection Failed! Check output console");
			e.printStackTrace();

		}

		if (connection != null) {
			//System.out.println("You made it, take control your database now!");
			
			Statement st = connection.createStatement();
			ResultSet rs;
			rs = st.executeQuery("SELECT \"idClaster\"  FROM \"Claster\" WHERE \"idPatent\" = "
					+ idDoc + ";");

			ArrayList<String> idClasters = new ArrayList<String>();
			while (rs.next()) {
				idClasters.add(rs.getString("idClaster"));
			}
			
			ArrayList<String> original = new ArrayList<String>();
			//попытка исправить хуйню в ответах
			for(int i=0; i<idClasters.size(); ++i)
			{
				original.addAll(getLecsemsFromClaster(getClasterFromBase(idClasters.get(i))));
			}
			
			

			rs.close();
			st.close();
			
			for(int i=0; i<idClasters.size(); ++i)
			{
				// поулчаем глоабльные кластеры, к которым относится данный патент
				st = connection.createStatement();
				rs = st.executeQuery("SELECT \"idGlobalClaster\"  FROM \"Mark\" WHERE \"idClaster\" = "
						+ idClasters.get(i) + ";");
	
				ArrayList<String> idGlobalClasters = new ArrayList<String>();
				while (rs.next()) {
					idGlobalClasters.add(rs.getString("idGlobalClaster"));
				}
	
				rs.close();
				st.close();
								
				// ищем кластеры из других патентов
				for(int j=0; j <idGlobalClasters.size(); ++j)
				{
					// поулчаем кластеры входящие в глобальный...
					st = connection.createStatement();
					rs = st.executeQuery("SELECT \"idClaster\" FROM \"Mark\" WHERE \"idGlobalClaster\" =" +
							idGlobalClasters.get(j)+";");
		
					ArrayList<String> idClastersC = new ArrayList<String>();
					while (rs.next()) {
						idClastersC.add(rs.getString("idClaster"));
					}
					
					for(int k=0; k<idClastersC.size(); ++k)
					{
						boolean flag = true;
						for(int l=0; l<idClasters.size(); ++l)
						{
							if (idClasters.get(l).equals(idClastersC.get(k)) == true) // если кластер принадлежит тому же патенту игнорим его
							{
								flag = false;
								break;
							}
						}
						
						if (flag == true) // если кластер из другого патента
						{
							Claster tmpC = getClasterFromBase(idClastersC.get(k));
							ArrayList<String> tmpL = getLecsemsFromClaster(tmpC);
							for(int ii=0; ii<tmpL.size(); ++ii)
							{
								boolean f = true;
								for(int jj=0; jj<original.size(); ++jj)
								{
									if (tmpL.get(ii).equals(original.get(jj)))
									{
										f = false;
										break;
									}							
									
								}
								if (f == true)
								{
									cs.add(tmpL.get(ii));
								}
							}
						}
						
					}
		
					rs.close();
					st.close();
				}
			}
			
			
			connection.close();
		}
		
		
		ArrayList<String> sort = cs;
		ArrayList<String> sortAll = new ArrayList<String>(new HashSet<String>(sort)); 
		Collections.sort(sortAll);
		
		ArrayList<String> result = new ArrayList<String>();
		for(int i=0; i<sortAll.size(); ++i)
		{
			int count=0;
			for(int j=0; j<cs.size(); ++j)
			{
				if(sortAll.get(i).equals(cs.get(j)))
				{
					count++;
				}
			}
			result.add(sortAll.get(i)+"\t"+Integer.toString(count));
			
		}
		
		return result;
	}
	
	public static String compareWithAllDoc(String iddoc) throws SQLException
	{
		Doc doc = getDocFromBase(iddoc);
		String res ="";
		try {

			Class.forName("org.postgresql.Driver");

		} catch (ClassNotFoundException e) {

			//System.out.println("Where is your PostgreSQL JDBC Driver? "
				//	+ "Include in your library path!");
			e.printStackTrace();

		}

		//System.out.println("PostgreSQL JDBC Driver Registered!");

		Connection conn = null;

		try {

			conn = DriverManager.getConnection(
					"jdbc:postgresql://127.0.0.1:5432/testENG", "postgres",
					"1234");

		} catch (SQLException e) {

			//System.out.println("Connection Failed! Check output console");
			e.printStackTrace();

		}

		if (conn != null) 
		{
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT \"idPatent\" FROM \"Patent\"");

			ArrayList<String> idDocs = new ArrayList<String>();
			while (rs.next()) {
				idDocs.add(rs.getString("idPatent"));
			}
			rs.close();
			st.close();
			
			for(int i=0; i<idDocs.size(); ++i)
			{
				System.out.println("Сравниваю "+(i+1)+" из "+idDocs.size());
				double tmp = getMarkOfSimilarityDoc(doc, getDocFromBase(idDocs.get(i)));
				res+=idDocs.get(i)+" - "+Double.toString(tmp)+"\n";
			}
				
			conn.close();
		}
		
		
		return res;
	}
	
	/**
	 * @param args
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws NoSuchAlgorithmException
	 * @throws SQLException
	 */
	public static void main(String[] args) throws MalformedURLException,
			IOException, ParserConfigurationException, SAXException,
			NoSuchAlgorithmException, SQLException {
		//Main app = new Main(); // Создаем экземпляр нашего приложения
		//app.setVisible(true); // С этого момента приложение запущено!
		/*
		int j = 64;
		for (int k = 1000055; k < 1000500; k++)
		{
			try
			{
			Doc test0 = getDocument("/home/alex/Рабочий стол/claims_semantic/" + Integer.toString(k) + ".txt", "testclass", "text" + Integer.toString(j));
			addDocToBase(test0);
		
		//Doc test0 = getDocument("/home/alex/Рабочий стол/claims_semantic/1000001.txt", "testclass", "text4");
		//addDocToBase(test0);
		//Doc test1 = getDocument("5_5.txt", "testclass", "text1");

		FileRW.writeFile("result.txt", "\nКонтекстные синонимы:\n");
		ArrayList<String> cs = getCS(Integer.toString(j));
		for(int i=0; i<cs.size(); ++i)
		{
			System.out.println(cs.get(i));
			FileRW.writeFileOver("result.txt", cs.get(i)+"\n");
		}
			
		FileRW.writeFileOver("result.txt", "\nСовпадения с патентами:\n");
		FileRW.writeFileOver("result.txt", compareWithAllDoc(Integer.toString(j)));
		j++;
			}
			catch(IOException ex)
			{
				System.out.println("ERRORRRRRRRRRRRRRRRR! ");
			}
		// остались получе
		}
		*/
	//	Claster tmpClaster = getClasterFromBase("98");
		//ArrayList<String> tmptmp = getLecsemsFromClaster(tmpClaster);
		
		//double[] test = getMarkOfClasters(test0.clasters.get(3), tmpClaster);

		//System.out.println("Jobs done! ");
		
		// в принципе робит
		//addDocToBase(test0);
		//addDocToBase(test1);
		//System.out.println("Jobs done! " + getMarkOfSimilarityDoc(getDocFromBase("32"), getDocFromBase("33")));
		
		
		
		// поулчение контекстных синонимов
		//ArrayList<String> cs = getCS("34");
		//for(int i=0; i<cs.size(); ++i)
		//{
		//	System.out.println(cs.get(i));
		//}
	
		//Doc test0 = getDocument("/home/alex/Рабочий стол/claims_semantic/100025.txt", "testclass", "text");
		//addDocToBase(test0);
		
		FileRW.writeFile("result.txt", "\nКонтекстные синонимы:\n");
		ArrayList<String> cs = getCS("29");
		for(int i=0; i<cs.size(); ++i)
		{
			System.out.println(cs.get(i));
			FileRW.writeFileOver("result.txt", cs.get(i)+"\n");
		}
			
		FileRW.writeFileOver("result.txt", "\nСовпадения с патентами:\n");
		FileRW.writeFileOver("result.txt", compareWithAllDoc("29"));
	}

}