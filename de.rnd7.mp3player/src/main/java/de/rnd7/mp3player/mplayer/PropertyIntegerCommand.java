package de.rnd7.mp3player.mplayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PropertyIntegerCommand extends MPlayerPropertyCommand<Integer> {

	private final Pattern pattern;
	private final String propertyName;

	public PropertyIntegerCommand(final String propertyName, final MPlayerThread player) {
		super(player);

		this.propertyName = propertyName;

		this.pattern = Pattern.compile(String.format("ANS_%s=(\\d+).*", propertyName), Pattern.CASE_INSENSITIVE);
	}

	@Override
	public Integer read() {
		return this.getPlayer()
				.sendPropertyRequest(this.propertyName)
				.stream()
				.map(this.pattern::matcher)
				.filter(Matcher::matches)
				.map(matcher -> matcher.group(1))
				.mapToInt(Integer::parseInt)
				.findFirst().orElse(-1);
	}

	@Override
	public void write(Integer chapter) {
		getPlayer().sendRequest(String.format("set_property %s %d\n", propertyName, chapter));
	}
}
