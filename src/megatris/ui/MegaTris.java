package megatris.ui;

import javax.swing.*;
import megatris.ai.AI;
import megatris.game.GameState;
import megatris.game.GameRules;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

public class MegaTris extends JFrame implements BoardViewState {
    // Game State
    private char[][][][] board = new char[3][3][3][3];
    private char[][] bigBoard = new char[3][3];
    private int activeBigRow = -1;
    private int activeBigCol = -1;
    private boolean isXTurn = true;
    private boolean gameOver = false;

    // Sound Components
    private VolumeSlider musicSlider;
    private VolumeSlider sfxSlider;

    // Settings Components
    private JToggleButton languageToggle;
    private ThemeSelector themeSelector;

    private final Preferences prefs = Preferences.userNodeForPackage(MegaTris.class);
    private boolean isEnglish = prefs.getBoolean("isEnglish", true);
    
    // UI Components
    private JPanel mainBoardPanel;
    private JPanel boardContainer;
    private JPanel controlPanel;
    private JPanel historyPanel;
    private JPanel historyContent;
    private JPanel historyRowsPanel;
    private JPanel historyNumbersColumn;
    private JPanel historyXColumn;
    private JPanel historyOColumn;
    private JLabel difficultyLabel;
    private JButton undoButton;
    private JButton[][][][] buttons = new JButton[3][3][3][3];
    private SubBoardPanel[][] bigPanels = new SubBoardPanel[3][3];
    private JLabel statusLabel;
    private OptionSlider opponentBox;
    private OptionSlider xComputerLevelBox;
    private OptionSlider oComputerLevelBox;
    private OptionSlider setupDifficultyBox;
    private JToggleButton humanSideToggle;
    private JLabel computerVersusLabel;
    private JButton computerBattleButton;
    private Component computerBattleStrut;
    private JButton restartButton;
    private boolean computerBattleRunning;
    private boolean gameStarted = true;
    private char humanSide = 'X';
    private JPanel screenPanel;
    private JPanel screenCards;
    private CardLayout screenLayout;
    private GameBackground animatedBackground;
    private JPanel mainMenuPanel;
    private JPanel modeMenuPanel;
    private JPanel setupMenuPanel;
    private JPanel settingsPanel;
    private JPanel multiplayerPanel;
    private JPanel gameScreen;
    private JLabel setupTitle;
    private boolean setupForComputerBattle;
    private MainBoardPanel mainBoardCanvas;
    private Timer animationTimer;
    private float selectionPhase;
    private float megaWinAnimation;
    private int[] megaWinningLine;
    private char megaWinner;
    private float[][] boardAnimation = new float[3][3];
    private final AI computer = new AI();
    private Timer computerTimer;
    private SwingWorker<AI.Move, Void> computerWorker;
    private Timer thinkingStatusTimer;
    private long thinkingStartedAt;
    private int lastBigRow = -1, lastBigCol = -1, lastRow = -1, lastCol = -1;
    private char lastMovePlayer = '\0';

    private JButton continueButton;
    private Component continueStrut;
    private String settingsReturnScreen = "menu";
    
    // History for UNDO
    private Stack<GameState> history = new Stack<>();
    private List<GameState> positionTimeline = new ArrayList<>();
    private List<String> moveTimeline = new ArrayList<>();
    private int viewedPosition = -1;

    // Theme Colors
    private Color bgMain = new Color(40, 42, 54);
    private Color bgHighlight = new Color(68, 71, 90);
    private Color bgWonX = new Color(20, 60, 40);
    private Color bgWonO = new Color(70, 20, 40);
    private Color fgX = new Color(139, 233, 253);
    private Color fgO = new Color(255, 121, 198);
    private Color fgDefault = Color.WHITE;
    private Color gridColor = new Color(98, 114, 164);
    private Color currentBorderColor = new Color(98, 114, 164); // Colore dinamico per i bordi

    public MegaTris() {
        super();
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLayout(new BorderLayout());

        // SALVA AUTOMATICAMENTE QUANDO CHIUDI LA FINESTRA DALLA 'X'
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                saveGameToDisk();
            }
        });

        SoundManager.playMusic("src/megatris/sound/menu.wav");

        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(24, 18, 24, 18));
        restartButton = new JButton("NEW GAME");
        restartButton.setName("NEW GAME");
        undoButton = new JButton("UNDO MOVE");
        undoButton.setName("UNDO MOVE");
        opponentBox = new OptionSlider(new String[]{"2 Players", "Computer - Beginner",
                "Computer - Medium", "Computer - Difficult", "Computer - Impossible",
                "Computer vs Computer"}, "");
        xComputerLevelBox = new OptionSlider(computerLevelNames(), "Player X");
        oComputerLevelBox = new OptionSlider(computerLevelNames(), "Player O");
        humanSideToggle = new JToggleButton("Play X");
        humanSideToggle.setName("PlayToggle");
        computerVersusLabel = new JLabel("VS");
        computerBattleButton = new JButton("START");
        computerBattleButton.setName("START");
        difficultyLabel = new JLabel("", SwingConstants.CENTER);
        UiStyler.minimalButton(restartButton);
        UiStyler.minimalButton(undoButton);
        UiStyler.minimalButton(computerBattleButton);
        UiStyler.minimalButton(humanSideToggle);
        undoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        UiStyler.buttonSize(restartButton, UiStyler.MENU_BUTTON_WIDTH, UiStyler.MENU_BUTTON_HEIGHT);
        UiStyler.buttonSize(undoButton, UiStyler.MENU_BUTTON_WIDTH, UiStyler.MENU_BUTTON_HEIGHT);
        UiStyler.buttonSize(humanSideToggle, UiStyler.MENU_BUTTON_WIDTH, UiStyler.MENU_BUTTON_HEIGHT);
        UiStyler.buttonSize(computerBattleButton, UiStyler.MENU_BUTTON_WIDTH, UiStyler.MENU_BUTTON_HEIGHT);
        undoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        computerBattleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        humanSideToggle.setAlignmentX(Component.CENTER_ALIGNMENT);
        UiStyler.width(opponentBox, 200);
        UiStyler.width(xComputerLevelBox, 220);
        UiStyler.width(oComputerLevelBox, 220);
        statusLabel = new JLabel("Player X Move", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton menuButton = menuButton("MENU", e -> showScreen("menu"));
        menuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton gameSettingsButton = menuButton("SETTINGS", e -> {
            settingsReturnScreen = "game";
            showScreen("settings");
        });
        gameSettingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        difficultyLabel.setForeground(fgDefault);
        difficultyLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        difficultyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        restartButton.addActionListener(e -> {
            SoundManager.playSound("src/megatris/sound/click.wav");
            if (isHumanVsComputer() && !gameStarted) startHumanGame();
            else restartGame();
        });
        undoButton.addActionListener(e -> {
            SoundManager.playSound("src/megatris/sound/click.wav");
            undoMove();
        });
        opponentBox.addActionListener(e -> {
            updateComputerBattleControls();
        });
        humanSideToggle.addActionListener(e -> {
            SoundManager.playSound("src/megatris/sound/click.wav");
            humanSide = humanSideToggle.isSelected() ? 'O' : 'X';
            
            humanSideToggle.setText(isEnglish ? "Play " + humanSide : "Gioca " + humanSide);
            
            if (isHumanVsComputer() && board != null) restartGame();
        });
        xComputerLevelBox.addActionListener(e -> {
            if (isComputerVsComputer() && board != null) restartGame();
        });
        oComputerLevelBox.addActionListener(e -> {
            if (isComputerVsComputer() && board != null) restartGame();
        });

        controlPanel.add(statusLabel);
        controlPanel.add(difficultyLabel);
        controlPanel.add(Box.createVerticalStrut(28));
        controlPanel.add(undoButton);
        controlPanel.add(Box.createVerticalStrut(12));
        controlPanel.add(restartButton);
        controlPanel.add(Box.createVerticalStrut(12));
        controlPanel.add(computerBattleButton);

        // SALVA LO SPAZIATORE NELLA VARIABILE
        computerBattleStrut = Box.createVerticalStrut(12);
        controlPanel.add(computerBattleStrut);
        
        // IL TUO NUOVO PULSANTE SETTINGS
        controlPanel.add(gameSettingsButton);
        controlPanel.add(Box.createVerticalStrut(12));
        
        controlPanel.add(menuButton);
        computerBattleButton.addActionListener(e -> {
            SoundManager.playSound("src/megatris/sound/click.wav");
            toggleComputerBattle();
        });

        // --- Main Board Panel ---
        mainBoardCanvas = new MainBoardPanel(this, new GridLayout(3, 3, 5, 5));
        mainBoardPanel = mainBoardCanvas;
        mainBoardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int br = 0; br < 3; br++) {
            for (int bc = 0; bc < 3; bc++) {
                SubBoardPanel bigPanel = new SubBoardPanel(this, br, bc);
                bigPanels[br][bc] = bigPanel;

                for (int sr = 0; sr < 3; sr++) {
                    for (int sc = 0; sc < 3; sc++) {
                        JButton btn = new JButton("");
                        btn.setFont(new Font("Arial", Font.BOLD, 24));
                        btn.setFocusPainted(false);
                        btn.setContentAreaFilled(false); 
                        btn.setOpaque(false);
                        btn.setBorderPainted(false); 
                        UiStyler.boardButton(btn);
                        
                        final int bRow = br, bCol = bc, sRow = sr, sCol = sc;
                        btn.addActionListener(new MoveListener(bRow, bCol, sRow, sCol));
                        
                        btn.setName("boardBtn");
                        buttons[br][bc][sr][sc] = btn;
                        bigPanel.add(btn);
                    }
                }
                mainBoardPanel.add(bigPanel);
            }
        }
        boardContainer = new JPanel(new GridBagLayout());
        boardContainer.add(mainBoardPanel);
        historyPanel = new JPanel(new BorderLayout(0, 12));
        historyPanel.setBorder(BorderFactory.createEmptyBorder(18, 8, 18, 8));
        historyContent = new JPanel(new BorderLayout(0, 4));
        historyContent.setOpaque(false);
        JPanel historyColumns = new JPanel();
        historyColumns.setLayout(new BoxLayout(historyColumns, BoxLayout.X_AXIS));
        historyColumns.setOpaque(false);
        historyNumbersColumn = historyColumn();
        historyXColumn = historyColumn();
        historyOColumn = historyColumn();
        historyNumbersColumn.add(historyCell("", false, -1, false, 0));
        historyXColumn.add(historyCell("X", true, -1, false, 1));
        historyOColumn.add(historyCell("O", true, -1, false, 2));
        historyColumns.add(historyNumbersColumn);
        historyColumns.add(historyXColumn);
        historyColumns.add(historyOColumn);
        historyRowsPanel = historyColumns;
        historyPanel.setOpaque(false);

        JPanel topAnchorWrapper = new JPanel(new BorderLayout());
        topAnchorWrapper.setOpaque(false);
        topAnchorWrapper.add(historyRowsPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(topAnchorWrapper);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setPreferredSize(new Dimension(6, 0));
        verticalBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(255, 255, 255, 40);
                this.trackColor = new Color(0, 0, 0, 0);
            }
            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {}
        });

        historyContent.add(scrollPane, BorderLayout.CENTER);
        historyPanel.add(historyContent, BorderLayout.CENTER);
        gameScreen = new JPanel(new BorderLayout(18, 0));
        gameScreen.setOpaque(false);
        gameScreen.setBackground(bgMain);
        gameScreen.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        gameScreen.add(historyPanel, BorderLayout.WEST);
        gameScreen.add(boardContainer, BorderLayout.CENTER);
        gameScreen.add(controlPanel, BorderLayout.EAST);
        boardContainer.setBackground(new Color(24, 26, 38));
        historyPanel.setBackground(new Color(bgMain.getRed(), bgMain.getGreen(),
            bgMain.getBlue(), 180));
        historyPanel.setPreferredSize(new Dimension(230, 0));
        historyPanel.setMinimumSize(new Dimension(230, 0));
        historyPanel.setMaximumSize(new Dimension(230, Integer.MAX_VALUE));
        controlPanel.setPreferredSize(new Dimension(320, 0));
        controlPanel.setMinimumSize(new Dimension(320, 0));
        controlPanel.setMaximumSize(new Dimension(320, Integer.MAX_VALUE));
        updateComputerBattleControls();
        boardContainer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeBoard();
            }
        });

        animationTimer = new Timer(40, e -> {
            selectionPhase = (selectionPhase + 2.5f) % 30f;
            boolean animating = false;
            for (int br = 0; br < 3; br++) {
                for (int bc = 0; bc < 3; bc++) {
                    if (boardAnimation[br][bc] < 1f) {
                        boardAnimation[br][bc] = Math.min(1f, boardAnimation[br][bc] + 0.08f);
                        animating = true;
                    }
                }
            }
            if (megaWinAnimation < 1f) {
                megaWinAnimation = Math.min(1f, megaWinAnimation + 0.06f);
                animating = true;
            }
            mainBoardPanel.repaint();
            if (!animating && gameOver) {
                mainBoardPanel.repaint();
            }
        });
        animationTimer.start();
        
        buildMenus();
        setTheme(prefs.get("theme", "Dark")); 
        initializeGame();
        loadGameFromDisk();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        applyThemeAndLanguage(getContentPane());
        updateUIState();
        
        showScreen("menu");
    }
    
    private void buildMenus() {
        screenLayout = new CardLayout();
        animatedBackground = new GameBackground();
        screenCards = new JPanel(screenLayout);
        screenCards.setOpaque(false);
        animatedBackground.setLayout(new BorderLayout());
        animatedBackground.add(screenCards, BorderLayout.CENTER);
        screenPanel = animatedBackground;
        mainMenuPanel = createMenuPanel();
        modeMenuPanel = createMenuPanel();
        setupMenuPanel = createMenuPanel();
        settingsPanel = createSettingsPanel();
        multiplayerPanel = createMenuPanel();

        // --- IMPOSTAZIONI: AUDIO, LINGUA E TEMI ---
        
        musicSlider = new VolumeSlider("Music Volume", SoundManager.getMusicVolumePercent());
        sfxSlider = new VolumeSlider("SFX Volume", SoundManager.getSfxVolumePercent());
        
        musicSlider.addActionListener(e -> {
            SoundManager.setMusicVolume(musicSlider.getValue() / 100f);
        });
        sfxSlider.addActionListener(e -> {
            SoundManager.setSfxVolume(sfxSlider.getValue() / 100f);
        });

        languageToggle = new JToggleButton();
        languageToggle.setName("Language");
        languageToggle.setSelected(!isEnglish); 
        languageToggle.setText(isEnglish ? "Language: English" : "Lingua: Italiano");
        
        UiStyler.minimalButton(languageToggle);
        UiStyler.buttonSize(languageToggle, 280, 44);
        languageToggle.setAlignmentX(Component.CENTER_ALIGNMENT);
        languageToggle.addActionListener(e -> {
            SoundManager.playSound("src/megatris/sound/click.wav");
            isEnglish = !languageToggle.isSelected();
            
            prefs.putBoolean("isEnglish", isEnglish);
            
            languageToggle.setText(isEnglish ? "Language: English" : "Lingua: Italiano");
            applyThemeAndLanguage(getContentPane()); 
        });

        String[] themeNames = {"Dark", "Light", "Neon", "Retro"};
        Color[] outerC = {new Color(40, 42, 54), new Color(240, 240, 240), new Color(15, 10, 30), new Color(40, 20, 10)};
        Color[] innerC = {new Color(139, 233, 253), new Color(0, 120, 215), new Color(255, 0, 255), new Color(255, 150, 0)};
        
        String savedTheme = prefs.get("theme", "Dark");
        int themeIdx = 0;
        for (int i = 0; i < themeNames.length; i++) {
            if (themeNames[i].equals(savedTheme)) themeIdx = i;
        }
        
        themeSelector = new ThemeSelector(themeNames, outerC, innerC, themeIdx, theme -> {
            setTheme(theme); 
        });
        themeSelector.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel settingsContent = new JPanel();
        settingsContent.setLayout(new BoxLayout(settingsContent, BoxLayout.Y_AXIS));
        settingsContent.setOpaque(false);
        settingsContent.add(musicSlider);
        settingsContent.add(Box.createVerticalStrut(12));
        settingsContent.add(sfxSlider);
        settingsContent.add(Box.createVerticalStrut(24));
        settingsContent.add(languageToggle);
        settingsContent.add(Box.createVerticalStrut(24));
        settingsContent.add(themeSelector);
        
        settingsPanel.add(centeredColumn("SETTINGS", settingsContent), BorderLayout.CENTER);

        // --- MENU PRINCIPALE ---
        JPanel mainContent = new JPanel();
        mainContent.setOpaque(false);
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.add(createLogoPanel());
        mainContent.add(Box.createVerticalStrut(42));
        
        mainContent.add(menuButton("NEW GAME", e -> showScreen("modes")));
        mainContent.add(Box.createVerticalStrut(16));

        // --- NUOVO TASTO: CONTINUA PARTITA ---
        continueButton = menuButton("CONTINUE GAME", e -> {
            showScreen("game");
            // Se avevi salvato mentre toccava al PC, sveglialo!
            if (isComputerTurn() && !gameOver) scheduleComputerMove();
        });
        continueButton.setName("CONTINUE GAME");
        continueStrut = Box.createVerticalStrut(16);
        continueButton.setVisible(false);
        continueStrut.setVisible(false);
        mainContent.add(continueButton);
        mainContent.add(continueStrut);        
        
        // MODIFICATO PER SALVARE CHE ARRIVI DAL MENU
        mainContent.add(menuButton("SETTINGS", e -> {
            settingsReturnScreen = "menu";
            showScreen("settings");
        }));
        
        mainContent.add(Box.createVerticalStrut(16));
        mainContent.add(menuButton("QUIT", e -> {
            saveGameToDisk(); // Salva prima di killare l'applicazione!
            System.exit(0);
        }));
        mainContent.add(Box.createVerticalStrut(42));
        
        JPanel creatorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        creatorPanel.setOpaque(false);
        creatorPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel createdBy = new JLabel("CREATED BY");
        createdBy.setForeground(fgDefault);
        createdBy.setFont(new Font("Arial", Font.PLAIN, 14));
        createdBy.setName("CREATED BY");
        JLabel psychen = new JLabel("PSYCHEN");
        psychen.setForeground(fgDefault);
        psychen.setFont(new Font("Arial", Font.BOLD, 14));
        psychen.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, fgDefault));
        psychen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        psychen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    SoundManager.playSound("src/megatris/sound/click.wav");
                    Desktop.getDesktop().browse(new java.net.URI("https://github.com/ItzPsychen"));
                } catch (Exception ex) {
                    System.out.println("Impossibile aprire il link: " + ex.getMessage());
                }
            }
        });
        creatorPanel.add(createdBy);
        creatorPanel.add(psychen);
        mainContent.add(creatorPanel);
        mainMenuPanel.add(centered(mainContent), BorderLayout.CENTER);

        // --- SELEZIONE MODALITA' ---
        JPanel modeContent = new JPanel();
        modeContent.setLayout(new BoxLayout(modeContent, BoxLayout.Y_AXIS));
        modeContent.setOpaque(false);
        modeContent.add(modeButton("2 PLAYERS", e -> startTwoPlayers()));
        modeContent.add(Box.createVerticalStrut(12));
        modeContent.add(modeButton("VS COMPUTER", e -> openComputerSetup(false)));
        modeContent.add(Box.createVerticalStrut(12));
        modeContent.add(modeButton("2 COMPUTERS", e -> openComputerSetup(true)));
        modeContent.add(Box.createVerticalStrut(12));
        modeContent.add(modeButton("MULTIPLAYER", e -> showScreen("multiplayer")));
        modeContent.add(Box.createVerticalStrut(12));
        modeContent.add(menuButton("BACK", e -> showScreen("menu")));
        modeMenuPanel.add(centeredColumn("NEW GAME", modeContent), BorderLayout.CENTER);

        // --- IMPOSTAZIONI PARTITA ---
        setupTitle = menuTitle("GAME SETUP");
        JPanel setupContent = new JPanel();
        setupContent.setOpaque(false);
        setupContent.setLayout(new BoxLayout(setupContent, BoxLayout.Y_AXIS));
        setupDifficultyBox = new OptionSlider(computerLevelNames(), "Difficulty");
        setupDifficultyBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        UiStyler.width(setupDifficultyBox, 280);
        xComputerLevelBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        oComputerLevelBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        UiStyler.width(xComputerLevelBox, 252);
        UiStyler.width(oComputerLevelBox, 252);
        
        setupContent.add(setupDifficultyBox);
        setupContent.add(Box.createVerticalStrut(12));
        setupContent.add(xComputerLevelBox);
        setupContent.add(Box.createVerticalStrut(12));
        setupContent.add(oComputerLevelBox);
        setupContent.add(Box.createVerticalStrut(18));
        humanSideToggle.setAlignmentX(Component.CENTER_ALIGNMENT);
        setupContent.add(humanSideToggle);
        setupContent.add(Box.createVerticalStrut(24));
        setupContent.add(menuButton("START GAME", e -> startConfiguredGame()));
        setupContent.add(Box.createVerticalStrut(12));
        setupContent.add(menuButton("BACK", e -> showScreen("modes")));
        setupMenuPanel.add(withBackPanel(setupTitle, setupContent), BorderLayout.CENTER);

        // --- MULTIGIOCATORE ---
        JPanel multiplayerContent = new JPanel();
        multiplayerContent.setOpaque(false);
        multiplayerContent.setLayout(new BoxLayout(multiplayerContent, BoxLayout.Y_AXIS));
        multiplayerContent.add(menuTitle("MULTIPLAYER"));
        multiplayerContent.add(Box.createVerticalStrut(24));
        JLabel unavailable = new JLabel("ONLINE PLAY COMING SOON");
        unavailable.setForeground(fgDefault);
        unavailable.setName("ONLINE PLAY COMING SOON");
        unavailable.setAlignmentX(Component.CENTER_ALIGNMENT);
        multiplayerContent.add(unavailable);
        multiplayerContent.add(Box.createVerticalStrut(28));
        multiplayerContent.add(menuButton("BACK", e -> showScreen("modes")));
        multiplayerPanel.add(centered(multiplayerContent), BorderLayout.CENTER);

        // --- ASSEGNAZIONE NOMI PER TRADUZIONE ---
        musicSlider.setName("Music Volume");
        sfxSlider.setName("SFX Volume");
        setupDifficultyBox.setName("Difficulty");
        opponentBox.setName("Opponent");

        xComputerLevelBox.setName("Player X");
        oComputerLevelBox.setName("Player O");

        // --- INSERIMENTO DELLE SCHERMATE ---
        screenCards.add(mainMenuPanel, "menu");
        screenCards.add(modeMenuPanel, "modes");
        screenCards.add(setupMenuPanel, "setup");
        screenCards.add(settingsPanel, "settings");
        screenCards.add(multiplayerPanel, "multiplayer");
        screenCards.add(gameScreen, "game");
        add(screenPanel, BorderLayout.CENTER);
        
        // --- COLLEGAMENTI TASTIERA ---
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("pressed LEFT"), "previousPosition");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("pressed RIGHT"), "nextPosition");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("typed <"), "previousPosition");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("typed >"), "nextPosition");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("pressed UP"), "firstPosition");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("pressed DOWN"), "lastPosition");
        getRootPane().getActionMap().put("previousPosition", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { browsePosition(-1); }
        });
        getRootPane().getActionMap().put("nextPosition", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { browsePosition(1); }
        });
        getRootPane().getActionMap().put("firstPosition", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { showPosition(0); }
        });
        getRootPane().getActionMap().put("lastPosition", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showPosition(positionTimeline.size() - 1);
            }
        });

        gameScreen.addComponentListener(new java.awt.event.ComponentAdapter() {});
    }

    private JPanel createLogoPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                String text = "MEGATRIS";
                
                java.util.Map<java.awt.font.TextAttribute, Object> attributes = new java.util.HashMap<>();
                attributes.put(java.awt.font.TextAttribute.TRACKING, 0.15);
                
                Font font = new Font("Arial", Font.BOLD, 85).deriveFont(attributes);
                g2.setFont(font);
                
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = fm.getAscent() + 10;

                g2.setColor(fgX);
                g2.drawString(text, x - 5, y - 4);

                g2.setColor(fgO);
                g2.drawString(text, x + 5, y + 4);

                g2.setColor(bgMain); 
                g2.drawString(text, x, y);

                String subText = "ULTIMATE TIC-TAC-TOE"; 
                java.util.Map<java.awt.font.TextAttribute, Object> subAttributes = new java.util.HashMap<>();
                subAttributes.put(java.awt.font.TextAttribute.TRACKING, 0.6); 
                
                Font subFont = new Font("Arial", Font.BOLD, 14).deriveFont(subAttributes);
                g2.setFont(subFont);
                
                FontMetrics subFm = g2.getFontMetrics();
                int subX = (getWidth() - subFm.stringWidth(subText)) / 2;
                int subY = y + subFm.getAscent() + 18; 

                g2.setColor(gridColor);
                g2.drawString(subText, subX, subY);
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(800, 160)); 
        panel.setMinimumSize(new Dimension(800, 160));
        panel.setMaximumSize(new Dimension(800, 160));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return panel;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));
        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JPanel controls = new JPanel(new GridBagLayout());
        controls.setOpaque(false);
        controls.setBorder(BorderFactory.createEmptyBorder(24, 80, 40, 80));
        controls.add(menuButton("BACK", e -> showScreen(settingsReturnScreen)),
                new GridBagConstraints(0, 0, 1, 1, 0, 1,
                        GridBagConstraints.CENTER, GridBagConstraints.SOUTH,
                        new Insets(0, 0, 0, 0), 0, 0));
                panel.add(controls, BorderLayout.SOUTH);
                return panel;
    }

    private JPanel centered(JComponent component) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(component);
        return wrapper;
    }

    private JPanel centeredColumn(String title, JComponent component) {
        JPanel content = new JPanel(new BorderLayout(0, 24));
        content.setOpaque(false);
        content.add(menuTitle(title), BorderLayout.NORTH);
        content.add(centered(component), BorderLayout.CENTER);
        return content;
    }

    private JLabel menuTitle(String text) {
        JLabel title = new JLabel(text, SwingConstants.CENTER);
        title.setName(text);
        title.setForeground(fgDefault);
        title.setFont(new Font("Arial", Font.BOLD, 34));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        return title;
    }

    private JButton menuButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setName(text);
        UiStyler.minimalButton(button);
        UiStyler.buttonSize(button, UiStyler.MENU_BUTTON_WIDTH, UiStyler.MENU_BUTTON_HEIGHT);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // PASSIAMO IL COLORE DINAMICO AL POSTO DI GRIDCOLOR
        UiStyler.buttonColors(button, fgDefault, bgMain, currentBorderColor); 
        
        button.addActionListener(e -> {
            SoundManager.playSound("src/megatris/sound/click.wav");
            listener.actionPerformed(e);
        });
        return button;
    }

    private JButton modeButton(String title, ActionListener listener) {
        JButton button = menuButton(title, listener);
        button.setPreferredSize(new Dimension(280, 60));
        button.setMinimumSize(new Dimension(280, 60));
        button.setMaximumSize(new Dimension(320, 60));
        return button;
    }

    private JPanel withBackPanel(JLabel title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout(0, 24));
        panel.setOpaque(false);
        panel.add(title, BorderLayout.NORTH);
        panel.add(centered(content), BorderLayout.CENTER);
        return panel;
    }

    private void showScreen(String screen) {
        // Se usciamo dalla schermata di gioco, fermiamo la CPU e mettiamo in pausa!
        if (!"game".equals(screen)) {
            if (computerBattleRunning) {
                computerBattleRunning = false;
                statusLabel.setText(translate("Computer battle paused"));
                statusLabel.setForeground(fgDefault);
            }
            cancelComputerMove();
        }
        
        // Se entriamo nel menu, controlliamo se c'è una partita attiva da poter continuare
        if ("menu".equals(screen)) {
            boolean hasActiveGame = gameStarted && !gameOver && positionTimeline.size() > 1;
            if (continueButton != null) {
                continueButton.setVisible(hasActiveGame);
                if (continueStrut != null) continueStrut.setVisible(hasActiveGame);
            }
        }

        screenLayout.show(screenCards, screen);
        
        if ("game".equals(screen)) {
            updateComputerBattleControls();
            updateUIState();
        }
        screenPanel.revalidate();
        screenPanel.repaint();
    }

    private void startTwoPlayers() {
        opponentBox.setSelectedIndex(0);
        initializeGame();
        showScreen("game");
    }

    private void openComputerSetup(boolean computerBattle) {
        setupForComputerBattle = computerBattle;
        
        String titleName = computerBattle ? "2 COMPUTERS" : "VS COMPUTER";
        setupTitle.setName(titleName);
        setupTitle.setText(translate(titleName));
        
        setupDifficultyBox.setVisible(!computerBattle);
        humanSideToggle.setVisible(!computerBattle);
        xComputerLevelBox.setVisible(computerBattle);
        oComputerLevelBox.setVisible(computerBattle);
        
        if (setupTitle.getParent() != null) {
            setupTitle.getParent().revalidate();
            setupTitle.getParent().repaint();
        }
        
        showScreen("setup");
    }

    private void startConfiguredGame() {
        if (setupForComputerBattle) {
            opponentBox.setSelectedIndex(5);
            initializeGame();
        } else {
            opponentBox.setSelectedIndex(setupDifficultyBox.getSelectedIndex() + 1);
            initializeGame();
            startHumanGame();
        }
        showScreen("game");
    }

    private GameState snapshot() {
        return new GameState(board, bigBoard, activeBigRow, activeBigCol, isXTurn, gameOver,
                megaWinningLine, megaWinner, lastBigRow, lastBigCol, lastRow, lastCol,
                lastMovePlayer);
    }

    private void recordPosition(char player, int bigRow, int bigCol, int row, int col) {
        positionTimeline.add(snapshot());
        moveTimeline.add((bigRow + 1) + ":" + (bigCol + 1)
                + " / " + (row + 1) + ":" + (col + 1));
        viewedPosition = positionTimeline.size() - 1;
        refreshHistoryView();
    }

    private void refreshHistoryView() {
        if (historyRowsPanel == null) return;
        historyNumbersColumn.removeAll();
        historyXColumn.removeAll();
        historyOColumn.removeAll();
        historyNumbersColumn.add(historyCell("", false, -1, false, 0));
        historyXColumn.add(historyCell("X", true, -1, false, 1));
        historyOColumn.add(historyCell("O", true, -1, false, 2));
        int rows = (moveTimeline.size() - 1 + 1) / 2;
        for (int row = 0; row < rows; row++) {
            int xIndex = 1 + row * 2;
            int oIndex = xIndex + 1;
            historyNumbersColumn.add(historyCell(String.valueOf(row + 1), false,
                    -1, false, 0));
            historyXColumn.add(historyCell(xIndex < moveTimeline.size()
                    ? moveTimeline.get(xIndex) : "", false, xIndex,
                    viewedPosition == xIndex, 1));
            historyOColumn.add(historyCell(oIndex < moveTimeline.size()
                    ? moveTimeline.get(oIndex) : "", false, oIndex,
                    viewedPosition == oIndex, 2));
        }
        historyNumbersColumn.revalidate();
        historyXColumn.revalidate();
        historyOColumn.revalidate();
        historyRowsPanel.revalidate();
        historyRowsPanel.repaint();
    }

    private JPanel historyColumn() {
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);
        return column;
    }

        private JLabel historyCell(String text, boolean header, int position,
            boolean selected, int column) {
        JLabel cell = new JLabel(text, header ? SwingConstants.CENTER : SwingConstants.LEFT);
        cell.setFont(header ? UiStyler.CONTROL_FONT : new Font("Arial", Font.PLAIN, 14));
        if (selected) {
            cell.setFont(header ? UiStyler.CONTROL_FONT : new Font("Arial", Font.BOLD, 14));
        }
        cell.setForeground(fgDefault);
        cell.setOpaque(true);
        cell.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        Color base = header ? bgHighlight : (column == 1 ? gridColor
            : column == 2 ? bgHighlight : bgMain);
        cell.setBackground(new Color(base.getRed(), base.getGreen(), base.getBlue(),
            selected ? 235 : 175));
        int width = column == 0 ? 32 : 88;
        cell.setPreferredSize(new Dimension(width, 24));
        cell.setMinimumSize(new Dimension(width, 24));
        cell.setMaximumSize(new Dimension(width, 24));
        if (position >= 0 && text != null && !text.isEmpty()) {
            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cell.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showPosition(position);
                }
            });
        }
        return cell;
    }

    private void browsePosition(int direction) {
        showPosition(viewedPosition + direction);
    }

    private void showPosition(int position) {
        if (positionTimeline.isEmpty()) return;
        int target = Math.max(0, Math.min(position, positionTimeline.size() - 1));
        
        if (target != positionTimeline.size() - 1 && computerBattleRunning) {
            computerBattleRunning = false;
            updateComputerBattleControls();
            statusLabel.setText(translate("Computer battle paused")); 
            statusLabel.setForeground(fgDefault);
        }

        if (isComputerVsComputer()) {
            cancelComputerMove();
        }

        restoreState(positionTimeline.get(target));
        viewedPosition = target;
        refreshHistoryView();
        updateUIState();
    }

    private void resizeBoard() {
        int size = Math.max(200, Math.min(boardContainer.getWidth(),
                boardContainer.getHeight()));
        
        if (size <= 200) size = 700;
        mainBoardPanel.setPreferredSize(new Dimension(size, size));
        mainBoardPanel.revalidate();
        boardContainer.revalidate();
        boardContainer.repaint();
    }

    private void initializeGame() {
        cancelComputerMove();
        computerBattleRunning = false;
        gameStarted = !isHumanVsComputer();
        board = new char[3][3][3][3];
        bigBoard = new char[3][3];
        activeBigRow = -1;
        activeBigCol = -1;
        isXTurn = true;
        gameOver = false;
        megaWinningLine = null;
        megaWinner = '\0';
        megaWinAnimation = 1f;
        boardAnimation = new float[3][3];
        lastBigRow = lastBigCol = lastRow = lastCol = -1;
        lastMovePlayer = '\0';
        history.clear();
        positionTimeline.clear();
        moveTimeline.clear();
        positionTimeline.add(snapshot());
        moveTimeline.add("START");
        viewedPosition = 0;
        refreshHistoryView();
        
        statusLabel.setForeground(fgDefault);
        updateComputerBattleControls();
        updateUIState();
        scheduleComputerMove();
    }

    private void restartGame() {
        initializeGame();
    }

    private void startHumanGame() {
        initializeGame();
        gameStarted = true;
        updateComputerBattleControls();
        updateUIState();
        scheduleComputerMove();
    }

    private void undoMove() {
        if (positionTimeline.size() <= 1) return;
        
        if (isHumanVsComputer() && humanSide == 'O' && positionTimeline.size() == 2) return;

        cancelComputerMove();

        int removeCount = 1; 
        
        if (isHumanVsComputer()) {
            if (isHumanTurn()) {
                removeCount = 2;
            } else {
                removeCount = 1;
            }
        }

        while (removeCount-- > 0 && positionTimeline.size() > 1) {
            positionTimeline.remove(positionTimeline.size() - 1);
            moveTimeline.remove(moveTimeline.size() - 1);
        }
        
        showPosition(positionTimeline.size() - 1);
        history.clear();
        
        if (isComputerTurn() && !gameOver) {
            scheduleComputerMove();
        }
    }

    private void restoreState(GameState lastState) {
        this.board = lastState.board;
        this.bigBoard = lastState.bigBoard;
        this.activeBigRow = lastState.activeBigRow;
        this.activeBigCol = lastState.activeBigCol;
        this.isXTurn = lastState.isXTurn;
        this.gameOver = lastState.gameOver;
        this.megaWinningLine = lastState.megaWinningLine == null
                ? null : lastState.megaWinningLine.clone();
        this.megaWinner = lastState.megaWinner;
        this.lastBigRow = lastState.lastBigRow;
        this.lastBigCol = lastState.lastBigCol;
        this.lastRow = lastState.lastRow;
        this.lastCol = lastState.lastCol;
        this.lastMovePlayer = lastState.lastMovePlayer;
        this.megaWinAnimation = 1f;
        this.boardAnimation = new float[3][3];
        for (int br = 0; br < 3; br++) {
            for (int bc = 0; bc < 3; bc++) {
                boardAnimation[br][bc] = bigBoard[br][bc] == '\0' ? 1f : 1f;
            }
        }
    }

    public boolean isBoardFull(int br, int bc) {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[br][bc][r][c] == '\0') return false;
            }
        }
        return true; 
    }

    private class MoveListener implements ActionListener {
        int bR, bC, sR, sC;
        public MoveListener(int bR, int bC, int sR, int sC) {
            this.bR = bR; this.bC = bC; this.sR = sR; this.sC = sC;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            processMove(bR, bC, sR, sC);
        }
    }

    private void processMove(int bR, int bC, int sR, int sC) {
        processMove(bR, bC, sR, sC, false);
    }

    private void processMove(int bR, int bC, int sR, int sC, boolean computerMove) {
        if (!gameStarted || gameOver || board[bR][bC][sR][sC] != '\0') return;
        if (viewedPosition != positionTimeline.size() - 1) return;
        if (!computerMove && !isHumanTurn()) return;
        if (activeBigRow != -1 && (bR != activeBigRow || bC != activeBigCol)) return;

        history.push(new GameState(board, bigBoard, activeBigRow, activeBigCol,
                isXTurn, gameOver, megaWinningLine, megaWinner,
                lastBigRow, lastBigCol, lastRow, lastCol, lastMovePlayer));
        char movePlayer = isXTurn ? 'X' : 'O';
        board[bR][bC][sR][sC] = movePlayer;
        lastBigRow = bR;
        lastBigCol = bC;
        lastRow = sR;
        lastCol = sC;
        lastMovePlayer = movePlayer;
        
        if (!computerMove) {
            SoundManager.playSound("src/megatris/sound/click.wav");
        }

        if (bigBoard[bR][bC] == '\0') {
            char smallWin = GameRules.checkWin(board[bR][bC]);
            if (smallWin != '\0') {
                bigBoard[bR][bC] = smallWin;
                boardAnimation[bR][bC] = 0f;
                char bigWin = GameRules.checkWin(bigBoard);
                if (bigWin == 'X' || bigWin == 'O') {
                    megaWinningLine = GameRules.winningLine(bigBoard, bigWin);
                    megaWinner = bigWin;
                    megaWinAnimation = 0f;
                    gameOver = true;
                    recordPosition(movePlayer, bR, bC, sR, sC);
                    showWinner(bigWin);
                    return;
                } else if (bigWin == 'D') {
                    gameOver = true;
                    recordPosition(movePlayer, bR, bC, sR, sC);
                    showDraw();
                    return;
                }
            }
        }

        if (bigBoard[sR][sC] != '\0' || isBoardFull(sR, sC)) {
            if (bigBoard[bR][bC] == '\0' && !isBoardFull(bR, bC)) {
                activeBigRow = bR;
                activeBigCol = bC;
            } else {
                activeBigRow = -1;
                activeBigCol = -1;
            }
        } else {
            activeBigRow = sR;
            activeBigCol = sC;
        }

        isXTurn = !isXTurn;
        recordPosition(movePlayer, bR, bC, sR, sC);
        updateUIState();
        scheduleComputerMove();
    }

    private void scheduleComputerMove() {
        if (!isComputerTurn() || gameOver || computerWorker != null) return;
        thinkingStartedAt = System.currentTimeMillis();
        
        String thinkingText = isEnglish ? "Computer is thinking" : "Il computer sta pensando";
        statusLabel.setText(thinkingText);
        statusLabel.setForeground(fgDefault);
        
        thinkingStatusTimer = new Timer(500, e -> {
            if (computerWorker != null) {
                long elapsed = System.currentTimeMillis() - thinkingStartedAt;
                int dots = (int) ((elapsed / 500) % 4);
                String dotString = "...".substring(0, dots);
                statusLabel.setText(thinkingText + dotString); 
            }
        });
        thinkingStatusTimer.start();
        computerTimer = new Timer(250, e -> {
            computerTimer = null;
            if (gameOver || !isComputerTurn() || computerWorker != null) return;
            final int requestedRow = activeBigRow;
            final int requestedCol = activeBigCol;
            final char computerPlayer = isXTurn ? 'X' : 'O';
            final AI.Difficulty difficulty = selectedDifficulty(computerPlayer);
            final long thinkTime = selectedThinkTimeMillis();
            computerWorker = new SwingWorker<AI.Move, Void>() {
                @Override
                protected AI.Move doInBackground() {
                    return computer.chooseMove(board, bigBoard, requestedRow, requestedCol,
                            computerPlayer, difficulty, thinkTime);
                }

                @Override
                protected void done() {
                    if (computerWorker != this) return;
                    computerWorker = null;
                    if (thinkingStatusTimer != null) {
                        thinkingStatusTimer.stop();
                        thinkingStatusTimer = null;
                    }
                    try {
                        AI.Move move = get();
                        if (move != null && !gameOver) {
                            int savedView = viewedPosition;
                            boolean wasViewingPast = savedView != positionTimeline.size() - 1;
                            
                            if (wasViewingPast) {
                                restoreState(positionTimeline.get(positionTimeline.size() - 1));
                                viewedPosition = positionTimeline.size() - 1;
                            }
                            
                            if (isComputerTurn()) {
                                processComputerMove(move);
                            }
                            
                            if (wasViewingPast) {
                                restoreState(positionTimeline.get(savedView));
                                viewedPosition = savedView;
                                refreshHistoryView();
                                updateUIState();
                            }
                        }
                    } catch (CancellationException ignored) {
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        statusLabel.setText("Computer move failed");
                    }
                }
            };
            computerWorker.execute();
        });
        computerTimer.setRepeats(false);
        computerTimer.start();
    }

    private void processComputerMove(AI.Move move) {
        SoundManager.playSound("src/megatris/sound/click.wav");
        processMove(move.bigRow, move.bigCol, move.row, move.col, true);
    }

    private void cancelComputerMove() {
        if (computerTimer != null) {
            computerTimer.stop();
            computerTimer = null;
        }
        if (computerWorker != null) {
            computerWorker.cancel(true);
            computerWorker = null;
        }
        if (thinkingStatusTimer != null) {
            thinkingStatusTimer.stop();
            thinkingStatusTimer = null;
        }
    }

    private boolean isHumanVsComputer() {
        int mode = opponentBox == null ? 0 : opponentBox.getSelectedIndex();
        return mode >= 1 && mode <= 4;
    }

    private boolean isComputerVsComputer() {
        return opponentBox != null && opponentBox.getSelectedIndex() == 5;
    }

    private boolean isComputerTurn() {
        if (isComputerVsComputer()) return computerBattleRunning;
        if (isHumanVsComputer()) {
            return gameStarted && ((humanSide == 'X' && !isXTurn)
                    || (humanSide == 'O' && isXTurn));
        }
        return false;
    }

    private void updateComputerBattleControls() {
        boolean visible = isComputerVsComputer();
        boolean humanComputerVisible = isHumanVsComputer();
        
        computerVersusLabel.setVisible(visible);
        computerBattleButton.setVisible(visible);
        
        // NASCONDE O MOSTRA LO SPAZIO IN BASE ALLA MODALITÀ
        if (computerBattleStrut != null) {
            computerBattleStrut.setVisible(visible);
        }
        
        if (computerBattleRunning) {
            computerBattleButton.setText(translate("PAUSE"));
        } else {
            computerBattleButton.setText(translate(positionTimeline.size() > 1 && !gameOver ? "RESUME" : "START"));
        }
        
        restartButton.setText(translate(humanComputerVisible && !gameStarted ? "START" : "NEW GAME"));
        computerVersusLabel.setForeground(fgDefault);
        controlPanel.revalidate();
        controlPanel.repaint();
    }

    private void toggleComputerBattle() {
        if (!isComputerVsComputer()) return;
        computerBattleRunning = !computerBattleRunning;
        
        if (computerBattleRunning) {
            computerBattleButton.setText(translate("PAUSE"));
            
            if (viewedPosition != positionTimeline.size() - 1) {
                showPosition(positionTimeline.size() - 1);
            }
            
            scheduleComputerMove();
        } else {
            computerBattleButton.setText(translate("RESUME"));
            cancelComputerMove();
            statusLabel.setText(translate("Computer battle paused"));
            statusLabel.setForeground(fgDefault);
        }
    }

    private boolean isHumanTurn() {
        return !isComputerTurn();
    }

    private String[] computerLevelNames() {
        return new String[]{"Beginner", "Medium", "Difficult", "Impossible"};
    }

    private AI.Difficulty selectedDifficulty(char player) {
        if (isComputerVsComputer()) {
            return difficultyFromIndex(player == 'X'
                    ? xComputerLevelBox.getSelectedIndex()
                    : oComputerLevelBox.getSelectedIndex());
        }
        switch (opponentBox.getSelectedIndex()) {
            case 1: return AI.Difficulty.BEGINNER;
            case 2: return AI.Difficulty.MEDIUM;
            case 3: return AI.Difficulty.DIFFICULT;
            case 4: return AI.Difficulty.IMPOSSIBLE;
            default: return AI.Difficulty.MEDIUM;
        }
    }

    private AI.Difficulty difficultyFromIndex(int index) {
        switch (index) {
            case 0: return AI.Difficulty.BEGINNER;
            case 1: return AI.Difficulty.MEDIUM;
            case 2: return AI.Difficulty.DIFFICULT;
            case 3: return AI.Difficulty.IMPOSSIBLE;
            default: return AI.Difficulty.MEDIUM;
        }
    }

    private long selectedThinkTimeMillis() {
        if (isComputerVsComputer()) {
            return thinkTimeForDifficulty(selectedDifficulty(isXTurn ? 'X' : 'O'));
        }
        switch (opponentBox.getSelectedIndex()) {
            case 2: return 1000L;
            case 3: return 2000L;
            case 4: return 4000L;
            default: return 100L;
        }
    }

    private long thinkTimeForDifficulty(AI.Difficulty difficulty) {
        switch (difficulty) {
            case MEDIUM: return 1000L;
            case DIFFICULT: return 2000L;
            case IMPOSSIBLE: return 4000L;
            default: return 100L;
        }
    }

    private void showWinner(char win) {
        statusLabel.setText("PLAYER " + win + " WINS!");
        statusLabel.setForeground(win == 'X' ? fgX : fgO);
        updateUIState();
    }
    
    private void showDraw() {
        statusLabel.setText("IT'S A DRAW!");
        statusLabel.setForeground(Color.GRAY);
        updateUIState();
    }

    private void updateUIState() {
        if (!gameStarted) {
            statusLabel.setText(isEnglish ? "Press New Game to begin" : "Premi Nuova Partita per iniziare");
            statusLabel.setForeground(fgDefault);
        } else if (gameOver) {
            if (megaWinner == 'X' || megaWinner == 'O') {
                statusLabel.setText(isEnglish ? ("PLAYER " + megaWinner + " WINS!") : ("IL GIOCATORE " + megaWinner + " VINCE!"));
                statusLabel.setForeground(megaWinner == 'X' ? fgX : fgO);
            } else {
                statusLabel.setText(isEnglish ? "IT'S A DRAW!" : "È UN PAREGGIO!");
                statusLabel.setForeground(Color.GRAY);
            }
        } else {
            char player = isXTurn ? 'X' : 'O';
            Color playerColor = isXTurn ? fgX : fgO;
            
            String prefix = isEnglish ? "Player " : "Turno di ";
            String suffix = isEnglish ? " Move" : "";
            
            statusLabel.setText("<html>" + prefix + "<font color='#" + colorToHex(playerColor) + "'>"
                    + player + "</font>" + suffix + "</html>");
            statusLabel.setForeground(fgDefault);
        }
        
        difficultyLabel.setText(gameDifficultyText());

        for (int br = 0; br < 3; br++) {
            for (int bc = 0; bc < 3; bc++) {
                bigPanels[br][bc].repaint(); 
                for (int sr = 0; sr < 3; sr++) {
                    for (int sc = 0; sc < 3; sc++) {
                        JButton btn = buttons[br][bc][sr][sc];
                        char cell = board[br][bc][sr][sc];
                        
                        if (cell == 'X') {
                            btn.setText("X");
                            btn.setForeground(fgX);
                        } else if (cell == 'O') {
                            btn.setText("O");
                            btn.setForeground(fgO);
                        } else {
                            btn.setText("");
                        }
                        boolean isLastMove = br == lastBigRow && bc == lastBigCol
                                && sr == lastRow && sc == lastCol;
                        btn.setBorderPainted(isLastMove);
                        btn.setBorder(isLastMove
                                ? new LineBorder(lastMovePlayer == 'X' ? fgX : fgO, 3)
                                : BorderFactory.createEmptyBorder());
                    }
                }
            }
        }
    }

    private String colorToHex(Color color) {
        return String.format("%06x", color.getRGB() & 0xFFFFFF);
    }

    private String gameDifficultyText() {
        if (isComputerVsComputer()) {
            return "X: " + translate((String)xComputerLevelBox.getSelectedItem())
                 + "   O: " + translate((String)oComputerLevelBox.getSelectedItem());
        }
        if (isHumanVsComputer()) {
            String diff = selectedDifficulty(isXTurn ? 'X' : 'O').name(); 
            diff = diff.substring(0, 1).toUpperCase() + diff.substring(1).toLowerCase();
            return "Computer: " + translate(diff);
        }
        return translate("2 PLAYERS");
    }

    public char[][][][] board() { return board; }
    public char[][] bigBoard() { return bigBoard; }
    public boolean gameOver() { return gameOver; }
    public int activeBigRow() { return activeBigRow; }
    public int activeBigCol() { return activeBigCol; }
    public boolean isXTurn() { return isXTurn; }
    public float selectionPhase() { return selectionPhase; }
    public float[][] boardAnimation() { return boardAnimation; }
    public Color bgMain() { return bgMain; }
    public Color bgHighlight() { return bgHighlight; }
    public Color bgWonX() { return bgWonX; }
    public Color bgWonO() { return bgWonO; }
    public Color fgX() { return fgX; }
    public Color fgO() { return fgO; }
    public Color gridColor() { return gridColor; }
    public int[] megaWinningLine() { return megaWinningLine; }
    public float megaWinAnimation() { return megaWinAnimation; }
    public char megaWinner() { return megaWinner; }
    public JPanel[][] bigPanels() { return bigPanels; }

    private void setTheme(String theme) {
        prefs.put("theme", theme);
        ThemePalette palette = ThemePalette.forName(theme);
        
        bgMain = palette.main;
        bgHighlight = palette.highlight;
        bgWonX = palette.wonX;
        bgWonO = palette.wonO;
        fgX = palette.x;
        fgO = palette.o;
        fgDefault = palette.foreground;
        gridColor = palette.grid;
        
        // --- LA VERA MODIFICA CORRETTA PER I BORDI ---
        if ("Neon".equals(theme) || "Retro".equals(theme)) {
            currentBorderColor = gridColor; 
        } else {
            currentBorderColor = new Color(98, 114, 164); 
        }
        
        getContentPane().setBackground(gridColor);
        if (screenPanel != null) screenPanel.setBackground(bgMain);
        if (gameScreen != null) gameScreen.setBackground(bgMain);
        if (mainMenuPanel != null) mainMenuPanel.setBackground(bgMain);
        if (modeMenuPanel != null) modeMenuPanel.setBackground(bgMain);
        if (setupMenuPanel != null) setupMenuPanel.setBackground(bgMain);
        if (multiplayerPanel != null) multiplayerPanel.setBackground(bgMain);
        if (settingsPanel != null) settingsPanel.setBackground(bgMain);
        
        if (controlPanel != null) {
            controlPanel.setBackground(new Color(bgMain.getRed(), bgMain.getGreen(), bgMain.getBlue(), 190));
        }
        if (historyPanel != null) {
            historyPanel.setBackground(new Color(bgMain.getRed(), bgMain.getGreen(), bgMain.getBlue(), 180));
        }
        
        if (mainBoardPanel != null) {
            mainBoardPanel.setBackground(gridColor);
            boardContainer.setOpaque(false);
            for (int br = 0; br < 3; br++) {
                for (int bc = 0; bc < 3; bc++) {
                    if (bigPanels[br][bc] != null) {
                        bigPanels[br][bc].setBorder(new LineBorder(gridColor, 3));
                        bigPanels[br][bc].repaint();
                    }
                }
            }
        }
        
        applyThemeAndLanguage(getContentPane());
        updateComputerBattleControls();
        updateUIState();
        refreshHistoryView();
        
        revalidate();
        repaint();
    }

    private String translate(String key) {
        if (isEnglish) return key; 
        switch(key) {
            case "NEW GAME": return "NUOVA PARTITA";
            case "SETTINGS": return "IMPOSTAZIONI";
            case "QUIT": return "ESCI";
            case "BACK": return "INDIETRO";
            case "2 PLAYERS": return "2 GIOCATORI";
            case "VS COMPUTER": return "VS COMPUTER";
            case "2 COMPUTERS": return "2 COMPUTER";
            case "MULTIPLAYER": return "MULTIGIOCATORE";
            case "START GAME": return "INIZIA GIOCO";
            case "UNDO MOVE": return "ANNULLA MOSSA";
            case "CREATED BY": return "CREATO DA";
            case "ONLINE PLAY COMING SOON": return "GIOCO ONLINE IN ARRIVO";
            case "GAME SETUP": return "IMPOSTAZIONI PARTITA";
            case "START": return "INIZIA";
            case "PAUSE": return "PAUSA";
            case "RESUME": return "RIPRENDI";
            case "MENU": return "MENU";
            case "CONTINUE GAME": return "CONTINUA PARTITA";
            
            case "Music Volume": return "Volume Musica";
            case "SFX Volume": return "Volume Effetti";
            case "Player X": return "Giocatore X";
            case "Player O": return "Giocatore O";
            case "Difficulty": return "Difficoltà";
            case "Opponent": return "Avversario";
            
            case "Beginner": return "Principiante";
            case "Medium": return "Medio";
            case "Difficult": return "Difficile";
            case "Impossible": return "Impossibile";

            case "Language": return "Lingua";
            case "Theme": return "Tema";
            case "Dark": return "Scuro";
            case "Light": return "Chiaro";
            case "Neon": return "Neon";
            case "Retro": return "Retro";
            
            case "Computer is thinking": return "Il computer sta pensando";
            case "Computer battle paused": return "Battaglia tra computer in pausa";
            case "Press New Game to begin": return "Premi Nuova Partita per iniziare";
            case "IT'S A DRAW!": return "È UN PAREGGIO!";
            default:
                if (key.startsWith("Player ") && key.endsWith(" Move")) {
                    return "Turno del " + key.replace("Player ", "Gioc. ").replace(" Move", "");
                }
                if (key.endsWith(" WINS!")) {
                    return "IL " + key.replace(" WINS!", " VINCE!").replace("PLAYER ", "GIOCATORE ");
                }
                return key;
        }
    }

    private void applyThemeAndLanguage(Container parent) {
        for (Component c : parent.getComponents()) {
            if (c instanceof AbstractButton) { 
                AbstractButton b = (AbstractButton) c;
                String name = b.getName();
                
                if (!"boardBtn".equals(name)) { 
                    // PASSIAMO IL COLORE DINAMICO AL POSTO DI GRIDCOLOR
                    UiStyler.buttonColors(b, fgDefault, bgMain, currentBorderColor);
                    
                    if ("Language".equals(name)) {
                        b.setText(isEnglish ? "Language: English" : "Lingua: Italiano");
                    } else if ("PlayToggle".equals(name)) {
                        b.setText(isEnglish ? "Play " + humanSide : "Gioca " + humanSide);
                    } else if (name != null) {
                        b.setText(translate(name));
                    }
                }
            } else if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                l.setForeground(fgDefault);
                if (l.getName() != null) l.setText(translate(l.getName()));
            } else if (c instanceof OptionSlider) {
                OptionSlider s = (OptionSlider) c;
                s.updateTheme(bgMain, bgHighlight, fgX, fgDefault);
                String sName = s.getName();
                
                if (sName != null) {
                    s.setPrefix(translate(sName));
                    
                    if (sName.equals("Difficulty") || sName.equals("Player X") || sName.equals("Player O")) {
                        s.setTranslatedOptions(new String[] {
                            translate("Beginner"), translate("Medium"), 
                            translate("Difficult"), translate("Impossible")
                        });
                    } 
                    else if (sName.equals("Opponent")) {
                        s.setTranslatedOptions(new String[] {
                            translate("2 PLAYERS"), 
                            "Computer - " + translate("Beginner"), 
                            "Computer - " + translate("Medium"), 
                            "Computer - " + translate("Difficult"), 
                            "Computer - " + translate("Impossible"), 
                            translate("2 COMPUTERS")
                        });
                    }
                }
            } else if (c instanceof VolumeSlider) {
                VolumeSlider s = (VolumeSlider) c;
                s.updateTheme(bgMain, bgHighlight, fgX, fgDefault);
                if (s.getName() != null) s.setPrefix(translate(s.getName()));
            } else if (c instanceof ThemeSelector) {
                ThemeSelector ts = (ThemeSelector) c;
                ts.updateTheme(fgDefault);
                ts.setPrefix(translate("Theme")); 
                
                ts.setTranslatedThemes(new String[] {
                    translate("Dark"),
                    translate("Light"),
                    translate("Neon"),
                    translate("Retro")
                });
            }
            
            if (c instanceof Container) {
                applyThemeAndLanguage((Container) c);
            }
        }

        updateUIState(); 
        updateComputerBattleControls(); 
    }

    private void saveGameToDisk() {
        try {
            java.io.File file = new java.io.File("megatris_save.dat");
            // Se la partita non è iniziata o è finita, cancella il salvataggio vecchio
            if (!gameStarted || positionTimeline.size() <= 1 || gameOver) {
                if (file.exists()) file.delete();
                return;
            }
            // Scrive letteralmente ogni variabile di gioco e la cronologia nel file!
            try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(new java.io.FileOutputStream(file))) {
                out.writeInt(opponentBox.getSelectedIndex());
                out.writeInt(setupDifficultyBox.getSelectedIndex());
                out.writeInt(xComputerLevelBox.getSelectedIndex());
                out.writeInt(oComputerLevelBox.getSelectedIndex());
                out.writeChar(humanSide);
                out.writeBoolean(setupForComputerBattle);
                
                out.writeObject(positionTimeline);
                out.writeObject(moveTimeline);
                out.writeInt(viewedPosition);
            }
        } catch (Exception e) {
            System.out.println("Salvataggio fallito: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadGameFromDisk() {
        try {
            java.io.File file = new java.io.File("megatris_save.dat");
            if (!file.exists()) return;
            // Legge il file e ripristina la partita!
            try (java.io.ObjectInputStream in = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
                opponentBox.setSelectedIndex(in.readInt());
                setupDifficultyBox.setSelectedIndex(in.readInt());
                xComputerLevelBox.setSelectedIndex(in.readInt());
                oComputerLevelBox.setSelectedIndex(in.readInt());
                humanSide = in.readChar();
                setupForComputerBattle = in.readBoolean();
                
                List<GameState> loadedPositions = (List<GameState>) in.readObject();
                List<String> loadedMoves = (List<String>) in.readObject();
                int loadedView = in.readInt();
                
                if (loadedPositions != null && !loadedPositions.isEmpty()) {
                    positionTimeline = loadedPositions;
                    moveTimeline = loadedMoves;
                    viewedPosition = loadedView;
                    gameStarted = true;
                    
                    humanSideToggle.setSelected(humanSide == 'O');
                    humanSideToggle.setText(isEnglish ? "Play " + humanSide : "Gioca " + humanSide);
                    
                    restoreState(positionTimeline.get(viewedPosition));
                    refreshHistoryView();
                }
            }
        } catch (Exception e) {
            System.out.println("Nessun salvataggio trovato o file obsoleto.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MegaTris().setVisible(true);
        });
    }
}