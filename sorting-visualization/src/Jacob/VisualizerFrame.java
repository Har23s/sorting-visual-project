package Jacob;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class VisualizerFrame extends JFrame {

	// ---------------- CONSTANTS ----------------
	// Define slider and visualization limits and default valuess
	private final int MAX_SPEED = 1000;
	private final int MIN_SPEED = 10;
	private final int MAX_SIZE = 500;
	private final int MIN_SIZE = 10;
	private final int DEFAULT_SPEED = 20;
	private final int DEFAULT_SIZE = 100;

	// Available sorting algorithms for selection
	private final String[] Sorts = {
		"Bubble", "Selection", "Insertion", "Gnome", "Merge", 
		"Radix LSD", "Radix MSD", "Shell", "Quandrix", 
		"Bubble(fast)", "Selection(fast)", "Insertion(fast)", "Gnome(fast)"
	};

	// ---------------- INSTANCE VARIABLES ----------------
	private int sizeModifier; // Used to scale bar heights relative to window size

	private JPanel wrapper;         // Main wrapper panel
	private JPanel arrayWrapper;     // Panel that displays the sorting bars
	private JPanel buttonWrapper;    // Panel that holds buttons, sliders, and dropdowns
	private JPanel[] squarePanels;   // Panels representing each element in the array
	private JButton start;           // Start sorting button
	private JComboBox<String> selection; // Dropdown for selecting sorting algorithm
	private JSlider speed;           // Slider for controlling sorting speed
	private JSlider size;            // Slider for controlling array size
	private JLabel speedVal;         // Label showing current speed value
	private JLabel sizeVal;          // Label showing current array size
	private GridBagConstraints c;    // Layout constraint object for arranging bars
	private JCheckBox stepped;       // Checkbox for stepped (evenly spaced) values

	// ---------------- CONSTRUCTOR ----------------
	public VisualizerFrame(){
		super("Sorting Visualizer"); // Title for the JFrame

		// Initialize all UI components
		start = new JButton("Start");
		buttonWrapper = new JPanel();
		arrayWrapper = new JPanel();
		wrapper = new JPanel();
		selection = new JComboBox<String>();
		speed = new JSlider(MIN_SPEED, MAX_SPEED, DEFAULT_SPEED);
		size = new JSlider(MIN_SIZE, MAX_SIZE, DEFAULT_SIZE);
		speedVal = new JLabel("Speed: 20 ms");
		sizeVal = new JLabel("Size: 100 values");
		stepped = new JCheckBox("Stepped Values");
		c = new GridBagConstraints();

		// Populate the sorting algorithm dropdown
		for(String s : Sorts) selection.addItem(s);

		// Layout for array display (bars)
		arrayWrapper.setLayout(new GridBagLayout());
		// Layout for the overall window
		wrapper.setLayout(new BorderLayout());

		// Define padding and alignment for each bar
		c.insets = new Insets(0,1,0,1);
		c.anchor = GridBagConstraints.SOUTH;

		// ---------------- EVENT HANDLERS ----------------

		// Start sorting when "Start" button is clicked
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SortingVisualizer.startSort((String) selection.getSelectedItem());
			}
		});

		// Toggle between random and stepped value generation
		stepped.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SortingVisualizer.stepped = stepped.isSelected();
			}
		});

		// Configure and listen for speed slider changes
		speed.setMinorTickSpacing(10);
		speed.setMajorTickSpacing(100);
		speed.setPaintTicks(true);
		speed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				speedVal.setText(("Speed: " + speed.getValue() + "ms"));
				validate();
				SortingVisualizer.sleep = speed.getValue(); // Update global delay
			}
		});

		// Configure and listen for size slider changes
		size.setMinorTickSpacing(10);
		size.setMajorTickSpacing(100);
		size.setPaintTicks(true);
		size.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				sizeVal.setText(("Size: " + size.getValue() + " values"));
				validate();
				SortingVisualizer.sortDataCount = size.getValue(); // Update array size
			}
		});

		// ---------------- LAYOUT CONSTRUCTION ----------------

		// Add controls to the bottom control panel
		buttonWrapper.add(stepped);
		buttonWrapper.add(speedVal);
		buttonWrapper.add(speed);
		buttonWrapper.add(sizeVal);
		buttonWrapper.add(size);
		buttonWrapper.add(start);
		buttonWrapper.add(selection);

		// Add panels to main wrapper
		wrapper.add(buttonWrapper, BorderLayout.SOUTH); // Controls at bottom
		wrapper.add(arrayWrapper);                      // Visualization at center

		// Add wrapper to JFrame
		add(wrapper);

		// Set window to full screen
		setExtendedState(JFrame.MAXIMIZED_BOTH);

		// Add listener to dynamically adjust bar heights when window is resized
		addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent e) {
				// Adjust height scaling factor dynamically based on window height
				sizeModifier = (int) ((getHeight() * 0.9) / (squarePanels.length));
			}

			@Override
			public void componentMoved(ComponentEvent e) { }
			@Override
			public void componentShown(ComponentEvent e) { }
			@Override
			public void componentHidden(ComponentEvent e) { }
		});

		// Display the window
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	// ---------------- METHODS ----------------

	/**
	 * preDrawArray() — Called before sorting starts.
	 * It initializes the bar panels based on the array valuess.
	 */
	public void preDrawArray(Integer[] squares){
		squarePanels = new JPanel[SortingVisualizer.sortDataCount];
		arrayWrapper.removeAll();

		// Compute height scaling (bars occupy 90% of window height)
		sizeModifier = (int) ((getHeight() * 0.9) / (squarePanels.length));

		// Create a bar for each element
		for(int i = 0; i < SortingVisualizer.sortDataCount; i++){
			squarePanels[i] = new JPanel();
			squarePanels[i].setPreferredSize(
				new Dimension(SortingVisualizer.blockWidth, squares[i] * sizeModifier)
			);
			squarePanels[i].setBackground(Color.blue); // Default color
			arrayWrapper.add(squarePanels[i], c);
		}

		// Redraw the window
		repaint();
		validate();
	}

	/**
	 * Re-draws array without color highlighting.
	 */
	public void reDrawArray(Integer[] x){
		reDrawArray(x, -1);
	}

	/**
	 * Re-draws array highlighting one element (working).
	 */
	public void reDrawArray(Integer[] x, int y){
		reDrawArray(x, y, -1);
	}

	/**
	 * Re-draws array highlighting two elements (working + comparing).
	 */
	public void reDrawArray(Integer[] x, int y, int z){
		reDrawArray(x, y, z, -1);
	}

	/**
	 * reDrawArray() — Updates array visualization with color-coded highlights:
	 * Green  = currently being worked on
	 * Red    = element being compared
	 * Yellow = element being read
	 * Blue   = default (idle)
	 */
	public void reDrawArray(Integer[] squares, int working, int comparing, int reading){
		arrayWrapper.removeAll();

		// Loop through each value and update bar appearance
		for(int i = 0; i < squarePanels.length; i++){
			squarePanels[i] = new JPanel();
			squarePanels[i].setPreferredSize(
				new Dimension(SortingVisualizer.blockWidth, squares[i] * sizeModifier)
			);

			// Assign colors based on current sorting operation
			if (i == working){
				squarePanels[i].setBackground(Color.green);
			}else if(i == comparing){
				squarePanels[i].setBackground(Color.red);
			}else if(i == reading){
				squarePanels[i].setBackground(Color.yellow);
			}else{
				squarePanels[i].setBackground(Color.blue);
			}
			arrayWrapper.add(squarePanels[i], c);
		}

		// Redraw updated array
		repaint();
		validate();
	}
}
