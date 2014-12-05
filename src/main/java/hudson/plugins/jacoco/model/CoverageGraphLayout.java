package hudson.plugins.jacoco.model;

import hudson.plugins.jacoco.Messages;
import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 * @author Martin Heinzerling
 */
public class CoverageGraphLayout
{
	enum CoverageType
	{
		INSTRUCTION(Messages.CoverageObject_Legend_Instructions())
				{
					@Override
					public Coverage getCoverage(CoverageObject<?> a)
					{
						return a.instruction;
					}

				},
		BRANCH(Messages.CoverageObject_Legend_Branch())
				{
					@Override
					public Coverage getCoverage(CoverageObject<?> a)
					{
						return a.branch;
					}

				},
		COMPLEXITY(Messages.CoverageObject_Legend_Complexity())
				{
					@Override
					public Coverage getCoverage(CoverageObject<?> a)
					{
						return a.complexity;
					}

				},
		METHOD(Messages.CoverageObject_Legend_Method())
				{
					@Override
					public Coverage getCoverage(CoverageObject<?> a)
					{
						return a.method;
					}

				},
		CLAZZ(Messages.CoverageObject_Legend_Class())
				{
					@Override
					public Coverage getCoverage(CoverageObject<?> a)
					{
						return a.clazz;
					}

				},
		LINE(Messages.CoverageObject_Legend_Line())
				{
					@Override
					public Coverage getCoverage(CoverageObject<?> a)
					{
						return a.line;
					}

				};

		private String message;

		CoverageType(String message)
		{
			this.message = message;
		}

		public String getMessage()
		{

			return message;
		}

		public abstract Coverage getCoverage(CoverageObject<?> a);

		public Number getValue(CoverageObject<?> a, CoverageValue value)
		{
			Coverage c = getCoverage(a);
			if (c == null) return 0;
			return value.getValue(c);

		}

	}

	enum CoverageValue
	{
		MISSED
				{
					@Override
					public String getMessage(CoverageType type)
					{
						return
								Messages.CoverageObject_Legend_Missed(type.getMessage());

					}

					@Override
					public Number getValue(Coverage c)
					{
						return c.getMissed();
					}
				},
		COVERED
				{
					@Override
					public String getMessage(CoverageType type)
					{
						return Messages.CoverageObject_Legend_Covered(type.getMessage());
					}

					@Override
					public Number getValue(Coverage c)
					{
						return c.getCovered();
					}
				},
		PERCENTAGE
				{
					@Override
					public String getMessage(CoverageType type)
					{
						return type.getMessage();
					}

					@Override
					public Number getValue(Coverage c)
					{
						return c.getPercentageFloat();
					}
				};


		public abstract String getMessage(CoverageType type);

		public abstract Number getValue(Coverage c);
	}

	static class Axis
	{
		private String label = null;

		public String getLabel()
		{
			return label;
		}
	}

	static class Plot
	{
		private CoverageValue value;
		private CoverageType type;
		private Axis axis;

		public Plot(Axis axis)
		{
			this.axis = axis;
		}

		public Number getValue(CoverageObject<?> a)
		{
			return type.getValue(a, value);
		}

		public String getMessage()
		{
			return value.getMessage(type);
		}

		public Axis getAxis()
		{
			return axis;
		}

		@Override
		public String toString()
		{
			return axis + " " + type + " " + value;
		}
	}

	private float baseStroke = 4f;
	private Stack<Axis> axes = new Stack<Axis>();
	private Stack<Plot> plots = new Stack<Plot>();

	public CoverageGraphLayout baseStroke(float baseStroke)
	{
		this.baseStroke = baseStroke;
		return this;
	}

	public CoverageGraphLayout axis()
	{
		axes.push(new Axis());
		return this;
	}

	private void assureAxis()
	{
		if (axes.isEmpty()) axis();
	}

	public CoverageGraphLayout label(String label)
	{
		assureAxis();
		axes.peek().label = label;
		return this;
	}

	public CoverageGraphLayout plot()
	{
		assureAxis();
		plots.add(new Plot(axes.peek()));
		return this;
	}

	private void assurePlot()
	{
		if (plots.isEmpty()) plot();
	}

	public CoverageGraphLayout type(CoverageType type)
	{
		assurePlot();
		plots.peek().type = type;
		return this;
	}

	public CoverageGraphLayout value(CoverageValue value)
	{
		assurePlot();
		plots.peek().value = value;
		return this;
	}

	public List<Axis> getAxes()
	{
		return Collections.unmodifiableList(axes);
	}

	public List<Plot> getPlots()
	{
		return Collections.unmodifiableList(plots);
	}

	public void apply(JFreeChart chart)
	{
		final CategoryPlot plot = chart.getCategoryPlot();
		Map<Axis, Integer> axisIds = new HashMap<Axis, Integer>();
		int axisId = 0;
		for (Axis axis : axes)
		{
			LineAndShapeRenderer renderer = new LineAndShapeRenderer(true, false);
			renderer.setBaseStroke(new BasicStroke(baseStroke));
			//add axis layout here
			plot.setRenderer(axisId, renderer);
			axisIds.put(axis, axisId);
			axisId++;
		}

		for (Plot p : plots)
		{
			axisId = axisIds.get(p.axis);
			int lineIdPerAxis = plot.getDataset(axisId).getRowIndex(p.getMessage());
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer(axisId);

			Color color = lineIdPerAxis == 0 ? Color.GREEN : Color.RED; //TODO

			renderer.setSeriesPaint(lineIdPerAxis, color);
			renderer.setSeriesItemLabelPaint(lineIdPerAxis, color);
			renderer.setSeriesFillPaint(lineIdPerAxis, color);
			//add line layout here
		}

		chart.getLegend().setPosition(RectangleEdge.RIGHT);
		chart.setBackgroundPaint(Color.white);
		// plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlinePaint(null);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);
		plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));
		// add common layout here
	}
}
