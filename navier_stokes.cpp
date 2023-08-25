#include <iostream>
#include <SFML/Graphics.hpp>
#include <cmath>
#include <vector>

template <typename T>
inline void util_swap(T& x, T& y)
{
	T tmp = x;
	x = y;
	y = tmp;
}

#define INDEX(x, y) (((y)*MAP_WIDTH)+(x))
#define WINDOW_WIDTH 720
#define WINDOW_HEIGHT 720
#define SCALE 4
#define MAP_WIDTH (WINDOW_WIDTH / SCALE)
#define MAP_HEIGHT (WINDOW_HEIGHT / SCALE)
#define SWAP(_x, _y) (util_swap((_x), (_y)))
#define ITERATIONS 10
#define VISCOSITY 0.0015
#define DIFFUSION_RATE 0.0

struct Cell
{
	sf::Vector2<double> velocity;
	double density;
};

Cell* cells = nullptr;
Cell* cells_prev = nullptr;
double* divergence = nullptr;
double* p = nullptr;

void boundary_conditions()
{
	for (int i = 0; i < MAP_WIDTH; i++)
	{
		cells[INDEX(i, 0)].velocity.y = -cells[INDEX(i, 1)].velocity.y;
		cells[INDEX(i, MAP_HEIGHT - 1)].velocity.y = -cells[INDEX(i, MAP_HEIGHT - 2)].velocity.y;

		cells[INDEX(i, 0)].density = cells[INDEX(i, 1)].density;
		cells[INDEX(i, MAP_HEIGHT - 1)].density = cells[INDEX(i, MAP_HEIGHT - 2)].density;

		divergence[INDEX(i, 0)] = divergence[INDEX(i, 1)];
		divergence[INDEX(i, MAP_HEIGHT - 1)] = divergence[INDEX(i, MAP_HEIGHT - 2)];

		p[INDEX(i, 0)] = p[INDEX(i, 1)];
		p[INDEX(i, MAP_HEIGHT - 1)] = p[INDEX(i, MAP_HEIGHT - 2)];
	}

	for (int i = 0; i < MAP_HEIGHT; i++)
	{
		cells[INDEX(0, i)].velocity.x = -cells[INDEX(1, i)].velocity.x;
		cells[INDEX(MAP_WIDTH - 1, i)].velocity.x = -cells[INDEX(MAP_WIDTH - 2, i)].velocity.x;

		cells[INDEX(0, i)].density = cells[INDEX(1, i)].density;
		cells[INDEX(MAP_WIDTH - 1, i)].density = cells[INDEX(MAP_WIDTH - 2, i)].density;

		divergence[INDEX(0, i)] = divergence[INDEX(1, i)];
		divergence[INDEX(MAP_WIDTH - 1, i)] = divergence[INDEX(MAP_WIDTH - 2, i)];

		p[INDEX(0, i)] = p[INDEX(1, i)];
		p[INDEX(MAP_WIDTH - 1, i)] = p[INDEX(MAP_WIDTH - 2, i)];
	}
}

void diffuse_density(double dt)
{
	double a = dt * DIFFUSION_RATE * (double)MAP_WIDTH * (double)MAP_HEIGHT;

	for (int k = 0; k < ITERATIONS; k++)
	{
		for (int y = 1; y < MAP_HEIGHT - 1; y++)
		{
			for (int x = 1; x < MAP_WIDTH - 1; x++)
			{
				int i = INDEX(x, y);
				int top = INDEX(x, y - 1);
				int bottom = INDEX(x, y + 1);
				int left = INDEX(x - 1, y);
				int right = INDEX(x + 1, y);

				cells[i].density = (cells_prev[i].density + a*(
					cells[left].density +
					cells[right].density +
					cells[top].density +
					cells[bottom].density
				) / 4.0) / (1.0 + a);
			}
		}

		boundary_conditions();
	}
}

void advect_density(double dt)
{
	for (int i = 1; i < MAP_WIDTH - 1; i++)
	{
		for (int j = 1; j < MAP_HEIGHT - 1; j++)
		{
			double x = i - dt * (double)MAP_WIDTH  * cells_prev[INDEX(i, j)].velocity.x;
			double y = j - dt * (double)MAP_HEIGHT * cells_prev[INDEX(i, j)].velocity.y;

			if (x < 1.0)
				x = 1.0;

			if (y < 1.0)
				y = 1.0;

			if (x > (double)MAP_WIDTH - 2.0)
				x = (double)MAP_WIDTH - 2.0;

			if (y > (double)MAP_HEIGHT - 2.0)
				y = (double)MAP_HEIGHT - 2.0;

			int i0 = (int)x;
			int j0 = (int)y;
			int i1 = i0 + 1;
			int j1 = j0 + 1;

			double s1 = x - i0;
			double s0 = 1 - s1;
			double t1 = y - j0;
			double t0 = 1 - t1;

			cells[INDEX(i, j)].density =
				s0 * (t0 * cells_prev[INDEX(i0, j0)].density + t1 * cells_prev[INDEX(i0, j1)].density) +
				s1 * (t0 * cells_prev[INDEX(i1, j0)].density + t1 * cells_prev[INDEX(i1, j1)].density);
		}
	}

	boundary_conditions();
}

void density_tick(double dt)
{
	SWAP(cells_prev, cells);
	diffuse_density(dt);

	SWAP(cells_prev, cells);
	advect_density(dt);
}

void project_velocity()
{
	for (int i = 1; i < MAP_WIDTH - 1; i++)
	{
		for (int j = 1; j < MAP_HEIGHT - 1; j++)
		{
			divergence[INDEX(i, j)] =
				-0.5 / (double)MAP_WIDTH  * (cells[INDEX(i + 1, j)].velocity.x - cells[INDEX(i - 1, j)].velocity.x) +
				-0.5 / (double)MAP_HEIGHT * (cells[INDEX(i, j + 1)].velocity.y - cells[INDEX(i, j - 1)].velocity.y);

			p[INDEX(i, j)] = 0.0;
		}
	}

	boundary_conditions();

	for (int k = 0; k < ITERATIONS; k++)
	{
		for (int i = 1; i < MAP_WIDTH - 1; i++)
		{
			for (int j = 1; j < MAP_HEIGHT - 1; j++)
			{
				p[INDEX(i, j)] = (divergence[INDEX(i, j)] +
					p[INDEX(i - 1, j)] + p[INDEX(i + 1, j)] +
					p[INDEX(i, j - 1)] + p[INDEX(i, j + 1)]) / 4.0;
			}
		}

		boundary_conditions();
	}

	for (int i = 1; i < MAP_WIDTH - 1; i++)
	{
		for (int j = 1; j < MAP_HEIGHT - 1; j++)
		{
			cells[INDEX(i, j)].velocity.x -= 0.5 * (p[INDEX(i + 1, j)] - p[INDEX(i - 1, j)]) * (double)MAP_WIDTH;
			cells[INDEX(i, j)].velocity.y -= 0.5 * (p[INDEX(i, j + 1)] - p[INDEX(i, j - 1)]) * (double)MAP_HEIGHT;
		}
	}

	boundary_conditions();
}

void diffuse_velocity(double dt)
{
	double a = dt * VISCOSITY * (double)MAP_WIDTH * (double)MAP_HEIGHT;

	for (int k = 0; k < ITERATIONS; k++)
	{
		for (int y = 1; y < MAP_HEIGHT - 1; y++)
		{
			for (int x = 1; x < MAP_WIDTH - 1; x++)
			{
				int i = INDEX(x, y);
				int top = INDEX(x, y - 1);
				int bottom = INDEX(x, y + 1);
				int left = INDEX(x - 1, y);
				int right = INDEX(x + 1, y);

				cells[i].velocity = (cells_prev[i].velocity + a*(
					cells[left].velocity +
					cells[right].velocity +
					cells[top].velocity +
					cells[bottom].velocity
				) / 4.0) / (1.0 + a);
			}
		}

		boundary_conditions();
	}
}

void advect_velocity(double dt)
{
	for (int i = 1; i < MAP_WIDTH - 1; i++)
	{
		for (int j = 1; j < MAP_HEIGHT - 1; j++)
		{
			double x = i - dt * (double)MAP_WIDTH  * cells_prev[INDEX(i, j)].velocity.x;
			double y = j - dt * (double)MAP_HEIGHT * cells_prev[INDEX(i, j)].velocity.y;

			if (x < 1.0)
				x = 1.0;

			if (y < 1.0)
				y = 1.0;

			if (x > (double)MAP_WIDTH - 2.0)
				x = (double)MAP_WIDTH - 2.0;

			if (y > (double)MAP_HEIGHT - 2.0)
				y = (double)MAP_HEIGHT - 2.0;

			int i0 = (int)x;
			int j0 = (int)y;
			int i1 = i0 + 1;
			int j1 = j0 + 1;

			double s1 = x - i0;
			double s0 = 1 - s1;
			double t1 = y - j0;
			double t0 = 1 - t1;

			cells[INDEX(i, j)].velocity =
				s0 * (t0 * cells_prev[INDEX(i0, j0)].velocity + t1 * cells_prev[INDEX(i0, j1)].velocity) +
				s1 * (t0 * cells_prev[INDEX(i1, j0)].velocity + t1 * cells_prev[INDEX(i1, j1)].velocity);
		}
	}

	boundary_conditions();
}

void velocity_tick(double dt)
{
	SWAP(cells_prev, cells);
	diffuse_velocity(dt);

	project_velocity();

	SWAP(cells_prev, cells);
	advect_velocity(dt);

	project_velocity();
}

void update(double dt)
{
	velocity_tick(dt);
	density_tick(dt);

	memcpy(cells_prev, cells, MAP_WIDTH * MAP_HEIGHT * sizeof(Cell));
}

int main()
{
	sf::RenderWindow window(sf::VideoMode(WINDOW_WIDTH, WINDOW_HEIGHT), "Navier Stokes");

	std::vector<sf::Vertex> pixels;

	for (int y = 0; y < MAP_HEIGHT; y++)
	{
		for (int x = 0; x < MAP_WIDTH; x++)
		{
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

	cells = new Cell[MAP_WIDTH * MAP_HEIGHT];
	cells_prev = new Cell[MAP_WIDTH * MAP_HEIGHT];
	divergence = new double[MAP_WIDTH * MAP_HEIGHT];
	p = new double[MAP_WIDTH * MAP_HEIGHT];

	memset(cells, 0, MAP_WIDTH * MAP_HEIGHT * sizeof(Cell));
	memset(cells_prev, 0, MAP_WIDTH * MAP_HEIGHT * sizeof(Cell));
	memset(divergence, 0, MAP_WIDTH * MAP_HEIGHT * sizeof(double));
	memset(p, 0, MAP_WIDTH * MAP_HEIGHT * sizeof(double));

	double delta_time = 0.0;
	sf::Clock delta_clock;

	int draw_mode = 1;

	sf::Vector2i mouse_pos, prev_mouse_pos;

	while (window.isOpen())
	{
		sf::Time dt = delta_clock.restart();
		delta_time = dt.asSeconds();

		sf::Event event;
		while (window.pollEvent(event)) {
			if (event.type == sf::Event::Closed) {
				window.close();
			}
		}

		if (sf::Mouse::isButtonPressed(sf::Mouse::Button::Left))
		{
			prev_mouse_pos = mouse_pos;
			mouse_pos = sf::Mouse::getPosition(window);

			mouse_pos /= SCALE;

			sf::Vector2i delta = mouse_pos - prev_mouse_pos;

			double len = sqrt(delta.x*delta.x + delta.y*delta.y);

			if (len >= 0.0001)
			{
				sf::Vector2<double> norm((double)delta.x / len, (double)delta.y / len);

				cells[INDEX(mouse_pos.x, mouse_pos.y)].velocity.x += norm.x * 500.0;
				cells[INDEX(mouse_pos.x, mouse_pos.y)].velocity.y += norm.y * 500.0;
			}
		}

		if (sf::Keyboard::isKeyPressed(sf::Keyboard::Space))
		{
			memset(cells, 0, MAP_WIDTH * MAP_HEIGHT * sizeof(Cell));
			memset(cells_prev, 0, MAP_WIDTH * MAP_HEIGHT * sizeof(Cell));
			memset(divergence, 0, MAP_WIDTH * MAP_HEIGHT * sizeof(double));
			memset(p, 0, MAP_WIDTH * MAP_HEIGHT * sizeof(double));

			for (int i = 0; i < MAP_WIDTH / 2; i++)
			{
				for (int j = 0; j < MAP_HEIGHT / 2; j++)
				{
					cells[INDEX(MAP_WIDTH/4 + i, MAP_HEIGHT/4 + j)].density = 1.0;
				}
			}
		}

		update(delta_time);

		for (int y = 0; y < MAP_HEIGHT; y++)
		{
			for (int x = 0; x < MAP_WIDTH; x++)
			{
				int idx = INDEX(x, y);

				double brightness = cells[idx].density;
				brightness = brightness / (1.0 + brightness);
				brightness *= 255.0;

				float r = brightness;
				float g = brightness;
				float b = brightness;

				double u = cells[idx].velocity.x;
				double v = cells[idx].velocity.y;

				u = abs(u);
				v = abs(v);

				u = u / (1.0 + u);
				v = v / (1.0 + v);

				u *= 255.0;
				v *= 255.0;

				r += u;
				g += v;
				//b += sqrt(u*u + v*v);

				sf::Color colour(
					(uint8_t)r,
					(uint8_t)g,
					(uint8_t)b
				);

				pixels[idx*4 + 0].color = colour;
				pixels[idx*4 + 1].color = colour;
				pixels[idx*4 + 2].color = colour;
				pixels[idx*4 + 3].color = colour;
			}
		}

		window.clear();
		window.draw(pixels.data(), pixels.size(), sf::Quads);
		window.display();
	}

	delete[] cells;
	delete[] cells_prev;
	delete[] divergence;
	delete[] p;

	return 0;
}
