package de.rnd7.mp3player.mplayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ReadBooleanCommand extends MPlayerCommand<Boolean>{

	private final Pattern pattern;
	private final String propertyName;

	public ReadBooleanCommand(final String propertyName, final MPlayerThread player) {
		super(player);

		this.propertyName = propertyName;
		this.pattern = Pattern.compile(String.format("ANS_%s=(no|yes|true|false).*", propertyName), Pattern.CASE_INSENSITIVE);
	}

	@Override
	public Boolean execute() {
		return this.getPlayer()
				.sendPropertyRequest(this.propertyName)
				.stream()
				.map(this.pattern::matcher)
				.filter(Matcher::matches)
				.map(matcher -> matcher.group(1))
				.findFirst()
				.map(String::toLowerCase)
				.map(this::toBoolean)
				.orElse(false);
	}

	private boolean toBoolean(final String s) {
		return s.equals("yes") || s.equals("true");
	}

}
