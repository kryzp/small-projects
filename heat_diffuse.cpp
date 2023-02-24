#include <SFML/Graphics.hpp>
#include <cmath>
#include <vector>

#define WINDOW_WIDTH 1280
#define WINDOW_HEIGHT 720
#define SCALE 8
#define MAP_WIDTH (WINDOW_WIDTH / SCALE)
#define MAP_HEIGHT (WINDOW_HEIGHT / SCALE)
#define HEAT_CONSTANT 0.15f

sf::Color gradient(float t)
{
	auto base = [&](float t) -> float { return std::expf(-1.0f * t * t); };
	return sf::Color(
		static_cast<char>(255.0f * base(t - 1.0f)),
		static_cast<char>(255.0f * base(t - 0.5f)),
		static_cast<char>(255.0f * base(t - 0.0f))
	);
}

int main()
{
	sf::RenderWindow window(sf::VideoMode(WINDOW_WIDTH, WINDOW_HEIGHT), "Heat Diffusion");

	float heatmap[MAP_WIDTH * MAP_HEIGHT] = {};
	bool paused = true;

	std::vector<sf::Vertex> pixels;

	for (int y = 0; y < MAP_HEIGHT; y++) {
		for (int x = 0; x < MAP_WIDTH; x++) {
			sf::Vertex tl = {}, tr = {}, bl = {}, br = {};
			float px = x * SCALE;
			float py = y * SCALE;
			tl.position = { px        , py         };
			tr.position = { px + SCALE, py         };
			bl.position = { px        , py + SCALE };
			br.position = { px + SCALE, py + SCALE };
			pixels.push_back(tl);
			pixels.push_back(tr);
			pixels.push_back(br);
			pixels.push_back(bl);
		}
	}

	while (window.isOpen())
	{
		sf::Event event;
		while (window.pollEvent(event)) {
			if (event.type == sf::Event::Closed) {
				window.close();
			}
		}

		if (sf::Keyboard::isKeyPressed(sf::Keyboard::Key::Enter)) {
			paused = false;
		} else if (sf::Keyboard::isKeyPressed(sf::Keyboard::Key::Space)) {
			paused = true;
		}

		int mx = sf::Mouse::getPosition(window).x / SCALE;
		int my = sf::Mouse::getPosition(window).y / SCALE;

		const float dT = 1.5f;

		if (sf::Mouse::isButtonPressed(sf::Mouse::Left)) {
			heatmap[my * MAP_WIDTH + mx] += dT;
		}

		if (sf::Mouse::isButtonPressed(sf::Mouse::Right)) {
			heatmap[my * MAP_WIDTH + mx] -= dT;
		}

		if (!paused) {
			float new_map[MAP_WIDTH * MAP_HEIGHT];
			for (int y = 0; y < MAP_HEIGHT; y++) {
				for (int x = 0; x < MAP_WIDTH; x++) {
					int idx = y * MAP_WIDTH + x;
					float mean = 0.f;
					int samples = 0;
					for (int p = -1; p <= 1; p++) {
						for (int q = -1; q <= 1; q++) {
							int xo = x + q;
							int yo = y + p;
							if (xo >= 0 && xo < MAP_WIDTH && yo >= 0 && yo < MAP_HEIGHT) {
								mean += heatmap[yo * MAP_WIDTH + xo];
								samples++;
							}
						}
					}
					new_map[idx] = std::lerp(heatmap[idx], mean / static_cast<float>(samples), HEAT_CONSTANT);
					pixels[idx*4 + 0].color = gradient(new_map[idx]);
					pixels[idx*4 + 1].color = gradient(new_map[idx]);
					pixels[idx*4 + 2].color = gradient(new_map[idx]);
					pixels[idx*4 + 3].color = gradient(new_map[idx]);
				}
			}

			memcpy(heatmap, new_map, sizeof(float) * MAP_WIDTH * MAP_HEIGHT);
		}

		window.clear();

		window.draw(pixels.data(), pixels.size(), sf::Quads);

		window.display();
	}

	return 0;
}
