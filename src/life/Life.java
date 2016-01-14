package life;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Life
{
    public static void main(String[] arglist) 
    {
        AppFrame App = new AppFrame();
        App.setVisible(true);
    }

    public static class AppFrame extends JFrame //Класс окна приложения
    {
        OptionScreen optionWindow = new OptionScreen();
        static int pauseTimer = 1;
        static int types = 8;
        static int worldSize = 220;
        static int cellSize = 3;

        static int cellMaxAge=10; //максимальный возраст
        static int youthAge = 4; //половозрелый возраст
        static int youthLife=8; //здоровья для размножения

        public AppFrame()
        {
            this.setTitle("GameOfLife");
            this.setSize(1280, 760);
            this.setLocationRelativeTo(null);
            this.setResizable(false);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            final Drawing component = new Drawing(); //создаём компонент для рисования
            component.Init();
            component.startGame();
            
            this.add(component); //добавляем компонент в окно приложения
            
            JToolBar toolBar = new JToolBar();
            JButton startButton = new JButton(" Пуск ");
            JButton stopButton = new JButton(" Пауза ");
            JButton resetButton = new JButton(" Ресет ");
            JButton optionsButton = new JButton(" Опции ");
            this.add(toolBar, BorderLayout.NORTH);
            toolBar.add(startButton);
            toolBar.add(stopButton);
            toolBar.add(resetButton);
            toolBar.addSeparator();
            toolBar.add(optionsButton);

            startButton.addActionListener((ActionEvent ae) -> {
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                component.startGame();
            });
            stopButton.addActionListener((ActionEvent ae) -> {
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                component.stopGame();
            });
            resetButton.addActionListener((ActionEvent ae) -> {
                component.Init();
                component.repaint();
            });
            optionsButton.addActionListener((ActionEvent ae) -> {
                optionWindow.setVisible(true);
            });
            
        }
    public static final class OptionScreen extends JFrame //Класс окна опций
    {
        final JPanel panel = new JPanel();
        
        public OptionScreen()
        {
            this.setTitle("Параметры");
            this.setSize(400, 400);
            this.setLocationRelativeTo(null);
            this.setResizable(false);
            this.add(panel);
            
            JLabel labelTimer = new JLabel("Длительность дня /мсек");
            JSpinner inputTimer = new JSpinner(new SpinnerNumberModel(pauseTimer,1,9999,1));
            JLabel labelTypes = new JLabel("Количество пород");
            JSpinner inputTypes = new JSpinner(new SpinnerNumberModel(types,1,8,1));
            JLabel labelWorldSize = new JLabel("Ширина мира /клеток");
            JSpinner inputWorldSize = new JSpinner(new SpinnerNumberModel(worldSize,2,380,1));
            JLabel labelCellSize = new JLabel("Размер клетки /пикс");
            JSpinner inputCellSize = new JSpinner(new SpinnerNumberModel(cellSize,1,30,1));
            JLabel labelMaxAge = new JLabel("Максимальный возраст клетки /дней");
            JSpinner inputMaxAge = new JSpinner(new SpinnerNumberModel(cellMaxAge,1,9999,1));
            JLabel labelAge = new JLabel("Половозрелый возраст /дней");
            JSpinner inputAge = new JSpinner(new SpinnerNumberModel(youthAge,1,9999,1));
            JLabel labelLife = new JLabel("Количество жизни для размножения /ед.");
            JSpinner inputLife = new JSpinner(new SpinnerNumberModel(youthLife,1,9999,1));
            JButton okButton = new JButton(" OK ");
            JButton cancelButton = new JButton(" Отмена ");

            panel.add(labelTimer,BorderLayout.WEST);
            panel.add(inputTimer);
            panel.add(labelTypes,BorderLayout.WEST);
            panel.add(inputTypes);
            panel.add(labelWorldSize,BorderLayout.WEST);
            panel.add(inputWorldSize);
            panel.add(labelCellSize,BorderLayout.WEST);
            panel.add(inputCellSize);
            panel.add(labelMaxAge,BorderLayout.WEST);
            panel.add(inputMaxAge);
            panel.add(labelAge,BorderLayout.WEST);
            panel.add(inputAge);
            panel.add(labelLife,BorderLayout.WEST);
            panel.add(inputLife);
            panel.add(okButton,BorderLayout.WEST);
            panel.add(cancelButton,BorderLayout.EAST);

            okButton.addActionListener((ActionEvent ae) -> {
                int a = (Integer) inputTimer.getValue();
                int b = (Integer) inputTypes.getValue();
                int c = (Integer) inputWorldSize.getValue();
                int d = (Integer) inputCellSize.getValue();
                int e = (Integer) inputMaxAge.getValue();
                int f = (Integer) inputAge.getValue();
                int g = (Integer) inputLife.getValue();
                setParameters(a,b,c,d,e,f,g);
                this.setVisible(false);
            });
            cancelButton.addActionListener((ActionEvent ae) -> {
                this.setVisible(false);
            });

        }
        
        
        public void setParameters(int timer,int types,int worldSize,int cellSize,int maxAge,int age,int life)
        {
            pauseTimer = timer;
            AppFrame.types = types;
            AppFrame.worldSize = worldSize;
            AppFrame.cellSize = cellSize;
            
            cellMaxAge=maxAge; //максимальный возраст
            youthAge = age; //половозрелый возраст
            youthLife=life; //здоровья для размножения
        }
    }
        /**
         * 
         */
        public class Drawing extends JComponent //компонент для рисования
        {
            private String LogOut;
            private volatile boolean running = false;
            public int currentX, currentY;
            public int iteration = 0; //Ход выполнения
            public int[] typesCounter = new int[types]; //счётчик количества экземпляров каждой породы
            public int[] deathCounter = new int[3]; // счётчик смертей по типам: 0-от голода, 1-от старости, 2-в бою
            public Cell[][] cell;
            Color cellColor[] = new Color[8];

            /**
             * Создаёт массив игрового поля
             */
            public Drawing() {
                this.cell = new Cell[worldSize][worldSize];
            }

            /**
             * 
             */
            private class Cell //Класс ячейки
            {
                public int life; //Жирнота клетки
                public int type; //травоядная, хищник
                public int age; //возраст клетки
                public int maxAge; //максимальный возраст клетки
                public int freeSpace = 0; //свободное место (0-нет, 1-вверх, 2-вниз, 3-влево, 4-вправо)
                public int enemyDirection = 0; //наличие врага (0-нет, 1-вверх, 2-вниз, 3-влево, 4-вправо)
                public boolean partner = false; //если есть сожители
                
                public int attack;
                public int defence;
                
                public Cell()
                {
                    
                }
            }    
            
            //Инициализация игрового поля с ячейками
            public void Init()
            {
                Arrays.fill(deathCounter, 0);
                cellColor[0]=Color.WHITE;
                cellColor[1]=Color.RED;
                cellColor[2]=Color.ORANGE;
                cellColor[3]=Color.YELLOW;
                cellColor[4]=Color.GREEN;
                cellColor[5]=Color.CYAN;
                cellColor[6]=Color.BLUE;
                cellColor[7]=Color.MAGENTA;
                iteration=0;
                for (int j=0; j<worldSize; j++)
                {
                    for (int i=0; i<worldSize; i++)
                    {
                        Random random = new Random();
                        int T = random.nextInt(types);
                        int L = random.nextInt(5)+5;
                        int A = random.nextInt(5)+1;
                        int MA= random.nextInt(cellMaxAge)+A;
                        cell[i][j]=new Cell();

                        if (T>0)
                        {
                            cell[i][j].life=L;
                            cell[i][j].type=T;
                            cell[i][j].age=A;
                            cell[i][j].maxAge=MA;
                            typesCounter[T]++;                            
                        }
                        else
                        {
                            cell[i][j].life=0;
                            cell[i][j].type=0;
                            cell[i][j].age=0;
                            cell[i][j].maxAge=0;
                            typesCounter[0]++;                            
                        }
                    }
                }
            }            
            
            private void startThread_1()
            {
                Thread thread;
                thread = new Thread(() -> {
                    while (Drawing.this.running)
                    {
                        if (Drawing.this.running)
                        {
                            iteration++;
                            printLog("ХОД-"+iteration);
                            Drawing.this.updateCells();
                            Drawing.this.repaint();
                            try {
                                Thread.sleep(pauseTimer);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Life.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                });
                thread.start();
            }
            
            public void printLog(String str)
            {
                //System.out.println(str);
            }
            
            public void updateCells() //расчёт каждой ячейки
            {
                for (int j = 0; j < worldSize; j++)
                 {
                     for (int i = 0; i < worldSize; i++)
                     {
                        Cell currentCell = cell[i][j];
                        currentX=i; currentY=j;
                        
                        LogOut =" Ячейка "+i+"-"+j;
                        LogOut +=" Тип-"+currentCell.type;
                        LogOut +=" Возраст-"+currentCell.age+" из "+currentCell.maxAge;
                        LogOut +=" Жир-"+currentCell.life;
                       //Работаем только с ненулевыми клетками
                         if (currentCell.type==0)
                         {
                             currentCell.age=0;
                             currentCell.life=0;
                         }
                         else
                         {
                             //Накидываем каждой живой клетке по одному году за ход
                            currentCell.age++;
                            //Посмотрим вокруг 
                             lookAround(i, j);
                            LogOut +=" Пространство-"+currentCell.freeSpace;
                             //Если есть пустое место
                             if (currentCell.freeSpace>0)
                             {
                                 currentCell.life++;
                                 //Если достигнута половозрелость и жизней хватает, то надо размножаться и биться
                                if (currentCell.age>youthAge && currentCell.life>youthLife)
                                {
                                    //Смотрим вокруг в поисках партнера для спаривания
                                    if (lookPartner(i, j)==true)
                                    {
                                        LogOut+=" Партнёр есть";
                                        //Рожаем на пустую клетку новый экземпляр
                                        newBorn(i, j);
                                    }
                                    //Смотрим вокруг в поисках врага и сражаемся с ним
                                    if (currentCell.enemyDirection>0)
                                    {
                                        cellFighting(i, j);
                                    }
                                }
                             }
                             else
                             {
                                currentCell.life--;
                             }
                            //Проверяем клетку на старость и голод
                            //Если слишком старая или голодная - всё обнуляем и оставляем пустым

                            if (currentCell.age>currentCell.maxAge)
                            {
                                death(i,j," Умерла от старости", 1);
                            }
                            
                            if (currentCell.life<=0)
                            {
                                death(i,j," Умерла от голода", 0);
                            }
                            printLog(LogOut);
                         }
                     }
                 }
            }
            
            //Смотрим вверх-вниз, влево-вправо. Выясняем где враги, где пустые места
            private void lookAround(int i, int j) 
            {
                //Это индексы для указания положений относительно указателя текущей ячейки
                int left, right, top, down;
                String str = "";
                left = i==0 ? worldSize-1 : i-1;
                right = i==worldSize-1 ? 0 : i+1;
                top = j==0 ? worldSize-1 : j-1;
                down = j==worldSize-1 ? 0 : j+1;
                
                Cell currentCell = cell[i][j];
      
                if (cell[right][j].type==0)
                {
                    currentCell.freeSpace=4;
                    LogOut +=" справа";
                }
                    else 
                    {
                        if (cell[right][j].type!=currentCell.type) currentCell.enemyDirection=4;
                    }
                if (cell[i][down].type==0)
                {
                    currentCell.freeSpace=2;
                    LogOut +=" внизу";
                }
                    else
                    {
                        if (cell[i][down].type!=currentCell.type) currentCell.enemyDirection=2;
                    } 
                if (cell[left][j].type==0)
                {
                    currentCell.freeSpace=3;
                    LogOut +=" слева";
                }
                    else
                    {
                        if (cell[left][j].type!=currentCell.type) currentCell.enemyDirection=3;
                    }
                if (cell[i][top].type==0) 
                {
                    currentCell.freeSpace=1;
                    LogOut +=" вверху";
                } 
                    else 
                    {
                        if (cell[i][top].type!=currentCell.type) currentCell.enemyDirection=1;
                    }
            }
            
            /**
             * Проверяет наличие клетки той же породы справа-слева, сверху-снизу
             * 
             * @param i координита по горизонтали
             * @param j координата по вертикали
             * @return true если партнёр есть, 
             *          false если партнёра нет.
             */
            private boolean lookPartner(int i, int j) 
            {
                //Это индексы для указания положений относительно указателя текущей ячейки
                int left, right, top, down;
                String str = "";
                left = i==0 ? worldSize-1 : i-1;
                right = i==worldSize-1 ? 0 : i+1;
                top = j==0 ? worldSize-1 : j-1;
                down = j==worldSize-1 ? 0 : j+1;
                
                Cell currentCell = cell[i][j];
                
                boolean p=false;
//                System.out.print(" Ищем партнёра");
                if (cell[left][j].type==currentCell.type||cell[right][j].type==currentCell.type||
                    cell[i][top].type==currentCell.type||cell[i][down].type==currentCell.type||
                    cell[left][top].type==currentCell.type||cell[right][top].type==currentCell.type||
                    cell[left][down].type==currentCell.type||cell[right][down].type==currentCell.type);
                {
                    p = true;
                }
                return p;
            }
            
            
            private void newBorn(int i, int j)
            {
                Random random = new Random();
                //Это индексы для указания положений относительно указателя текущей ячейки
                int left, right, top, down;
                String str = "";
                left = i==0 ? worldSize-1 : i-1;
                right = i==worldSize-1 ? 0 : i+1;
                top = j==0 ? worldSize-1 : j-1;
                down = j==worldSize-1 ? 0 : j+1;

                Cell currentCell = cell[i][j];
                Cell newCell;
                switch (currentCell.freeSpace)
                {
                    case 1:
                    {
                        newCell=cell[i][top];
                        LogOut+=" Размножилась вверх ";
                        break;
                    }
                    case 2:
                    {
                        newCell = cell[i][down];
                        LogOut+=" Размножилась вниз ";
                        break;
                    }
                    case 3:
                    {
                        newCell = cell[left][j];
                        LogOut+=" Размножилась влево ";
                        break;
                    }
                    case 4:
                    {
                        newCell = cell[right][j];
                        LogOut+=" Размножилась вправо ";
                        break;
                    }
                    default: 
                    {
                        newCell=null;
                    }
                }
                    newCell.type = currentCell.type;
                    newCell.life=10;
                    newCell.age=1;
                    newCell.maxAge=random.nextInt(cellMaxAge)+youthAge;
                    //currentCell.cell_value=1;
                    //currentCell.cell_age=1;

            }

            /**
             * Процедура битвы между клеткой и её противником
             * Победа или поражение определяются значениями поддержки и здоровья
             * вычисляемых в reinforcement()
             * По результату боя клетка приобретает породу победителя, 
             * половину от здоровья побежденного и возраст 1
             * 
             * @param i координата по горизонтали
             * @param j координата по вертикали
             */
            private void cellFighting(int i, int j)
            {
                Cell currentCell = cell[i][j];
                Cell enemyCell;

                int left, right, top, down;
                left = i==0 ? worldSize-1 : i-1;
                right = i==worldSize-1 ? 0 : i+1;
                top = j==0 ? worldSize-1 : j-1;
                down = j==worldSize-1 ? 0 : j+1;
                boolean isAlive;

                switch(currentCell.enemyDirection)
                {
                    default: {isAlive=false;}
                    case 1:{
                        enemyCell=cell[i][top];
                        isAlive = reinforcement(i,j)>reinforcement(i,top);
                        break;
                    }
                    case 2:{
                        enemyCell=cell[i][down];
                        isAlive = reinforcement(i,j)>reinforcement(i,down);
                        break;
                    }
                    case 3:{
                        enemyCell=cell[left][j];
                        isAlive = reinforcement(i,j)>reinforcement(left,j);
                        break;
                    }
                    case 4:{
                        enemyCell=cell[right][j];
                        isAlive = reinforcement(i,j)>reinforcement(right,j);
                        break;
                    }
                }
                
                if (isAlive == true)
                {
                    enemyCell.type=currentCell.type;
                    enemyCell.life=(Integer)enemyCell.life/2;
                    enemyCell.age=1;
                }
                else
                {
                    death(i,j, " Погибла в драке", 2);
                    currentCell.type=enemyCell.type;
                    currentCell.life=enemyCell.life/2;
                    currentCell.age=1;
                }
                currentCell.enemyDirection=0;
            }
            
            /**
             * Процедура проверки окружающего пространства на предмет поддержки своей породы
             * Чем больше вокруг "своих", тем больше атакующий потенциал клетки
             * @param i координата по горизонтали
             * @param j координата по вертикали
             * @return сумму поддерживающих клеток и собственного здоровья клетки
             */
            private int reinforcement(int i, int j)
            {
                int reinforcement=0;
                int left, right, top, down;
                left = i==0 ? worldSize-1 : i-1;
                right = i==worldSize-1 ? 0 : i+1;
                top = j==0 ? worldSize-1 : j-1;
                down = j==worldSize-1 ? 0 : j+1;

                if (cell[i][top].type==cell[i][j].type) {reinforcement++;}
                if (cell[i][down].type==cell[i][j].type) {reinforcement++;}
                if (cell[left][j].type==cell[i][j].type) {reinforcement++;}
                if (cell[right][j].type==cell[i][j].type) {reinforcement++;}
                if (cell[left][top].type==cell[i][j].type) {reinforcement++;}
                if (cell[right][top].type==cell[i][j].type) {reinforcement++;}
                if (cell[left][down].type==cell[i][j].type) {reinforcement++;}
                if (cell[right][down].type==cell[i][j].type) {reinforcement++;}
                
                return reinforcement+cell[i][j].life;
            }
            
            
            
            
            /**
             * Смерть ячейки             * 
             * @param i координата по горизонтали
             * @param j координата по вертикали
             * @param reason причина смерти для лога.
             * @param reasonCounter причина смерти для счётчика
             * 0-от голода, 1-от старости, 2-в бою
             */
            public void death(int i, int j, String reason, int reasonCounter)
            {
                cell[i][j].type=0;
                deathCounter[reasonCounter]++;
                printLog("Клетка "+i+"-"+j+reason);
            }
            
            @Override
            protected void paintComponent(Graphics g) //вывод на экран всего массива ячеек
            {
                Arrays.fill(typesCounter,0);
                for (int j = 0; j < worldSize; j++)
                {
                    for (int i = 0; i < worldSize; i++)
                    {
                        typesCounter[cell[i][j].type]++;
                        int xPos = 200+(i*cellSize)+(cellSize/2);
                        int yPos = (j*cellSize)+(cellSize/2);
                        Color C; 
                            float brightness;
                            float hue;
                            float saturation;
                        //Цвет КРАЯ ячейки
                        hue = (float) 1/cell[i][j].type;
                        brightness = (float) cellMaxAge/cell[i][j].age;
                        saturation = 1f;
                        C = Color.getHSBColor(hue,saturation,brightness);
                        //C = cellColor[cell[i][j].type];
                        g.setColor(C);
                        g.fillRect(xPos, yPos, cellSize, cellSize);
//                        g.setColor(C);
//                        g.drawOval(xPos, yPos, cell[i][j].life, cell[i][j].life);
                    }
                }
                paintHud(g);
            }

            public void paintHud(Graphics g)
            {
                g.setColor(Color.black);
                g.drawRect(0, 0, 200, 400);
                g.drawRect(2, 2, 196, 396);

                g.drawString("День-"+String.valueOf(iteration),15,13);
                
                //Число каждой породы и цветная полоска соответствующей длины
                for (int t=1; t<types; t++)
                {
                    int barWidth = typesCounter[t]*150/(worldSize*worldSize)+5;
                    g.setColor(cellColor[t]);
                    
                    g.fillRect(30,15+t*17,barWidth,15);
                    g.setColor(Color.black);
                    g.drawString(String.valueOf(typesCounter[t]),5,28+t*17);
                }
                //Число смертей каждого типа
                for (int t=0; t<3; t++)
                {
                    g.setColor(Color.black);
                    g.drawString(String.valueOf(deathCounter[t]),5,250+t*17);
                }
                
            }

            public void startGame()
            {
                this.running = true;
                this.startThread_1();
                this.repaint();
            }

            public void stopGame()
            {
                this.running = false;
                this.repaint();
            }

        }
    }
}

